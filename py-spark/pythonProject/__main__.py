import argparse
import logging
import uuid
from queue import Queue

import requests
import pyarrow as pa
from pyspark.sql import SparkSession
from urllib.parse import urljoin

from src.DatabaseStrategy import DatabaseFactory
from src.arrow_uploader import ArrowUploader

# 初始化日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Config:
    def __init__(self, **kwargs):
        self.dbtype = kwargs.get('dbtype', '')
        self.host = kwargs.get('host', '')
        self.port = kwargs.get('port', 0)
        self.database = kwargs.get('database', '')
        self.endpoint = kwargs.get('endpoint', '')
        self.accesskey = kwargs.get('accesskey', '')
        self.secretkey = kwargs.get('secretkey', '')
        self.username = kwargs.get('username', '')
        self.password = kwargs.get('password', '')
        self.query = kwargs.get('query', '')
        self.filename = kwargs.get('filename', '')
        self.serverip = kwargs.get('serverip', '')
        self.serverport = kwargs.get('serverport', 0)


def parse_args():
    parser = argparse.ArgumentParser(description="DynamicDatabaseJob 1.0")

    parser.add_argument("--dbtype", type= str, required=True, help="Database type (oracle, mysql, postgresql)")
    parser.add_argument("--host", type= str, required=True, help="Datasource host")
    parser.add_argument("--port", type=int, required=True, help="Datasource port")
    parser.add_argument("--database", type=str, help="Database name")
    parser.add_argument("--endpoint", type= str, help="MinIO endpoint")
    parser.add_argument("--accesskey", type= str, help="OSS accesskey")
    parser.add_argument("--secretkey", type= str, help="OSS secretkey")
    parser.add_argument("--serverip", type= str, help="Data server IP")
    parser.add_argument("--serverport", type=int, help="Data server port")
    parser.add_argument("--username", type= str, required=True, help="Database username")
    parser.add_argument("--password", type= str, required=True, help="Database password")
    parser.add_argument("--query", type= str, required=True, help="SQL query to execute")
    parser.add_argument("--filename", type= str, required=True, help="Filename located in OSS")

    args = parser.parse_args()
    return Config(**vars(args))


def run_spark_job(config):
    spark = SparkSession.builder.appName("Spark Database Job").getOrCreate()
    strategy = DatabaseFactory.get_strategy(
        config.dbtype, config.host, config.port, config.database, config.username, config.password
    )
    minio_bucket = "data-service"
    try:
        df = spark.read \
            .format("jdbc") \
            .option("url", strategy.get_jdbc_url()) \
            .option("dbtable", f"({config.query.strip(';')}) dbtable") \
            .option("driver", strategy.get_driver()) \
            .load()
        print("DataFrame head:\n", df.head())
        logger.info("DataFrame loaded successfully.")

        # 计算总行数
        total_rows = df.count()
        logger.info(f"Total rows: {total_rows}")

        # 调整分区数
        total_data_size_gb = df.count() * df.first().__sizeof__() / (1024 * 1024 * 1024)
        optimal_partitions = calculate_optimal_partitions(spark, total_data_size_gb)
        logger.info(f"Optimal number of partitions: {optimal_partitions}")
        # 重新分区
        df = df.repartition(100)

        # 使用 mapPartitions 收集分区处理结果
        partition_urls = df.rdd.mapPartitions(
            lambda partition: process_partition(partition, config, minio_bucket, batch_size=10000)
        ).collect()
        # 通知服务端
        notify_server_of_completion(config, partition_urls)
    except Exception as e:
        logger.error(f"Error writing DataFrame to MinIO: {e}")
    finally:
        spark.stop()

def process_partition(partition, config, minio_bucket, batch_size=10):
    """处理每个分区，将所有批次合并为一个 Arrow 表后上传到 MinIO，并返回预签名 URL。"""
    rows = list(partition)
    if not rows:
        logger.info("Empty partition, skipping.")
        return iter([])  # 空分区返回空迭代器

    # 提取所有可能的字段
    all_columns = set()
    for row in rows:
        all_columns.update(row.asDict().keys())
    logger.info(f"Extracted columns: {all_columns}")

    # 初始化存储所有批次数据的字典
    combined_data = {col: [] for col in all_columns}

    # 分区内批次处理
    for i in range(0, len(rows), batch_size):
        batch_rows = rows[i:i + batch_size]
        logger.info(f"Processing batch {i // batch_size + 1} of {len(rows) // batch_size + 1}")
        for row in batch_rows:
            row_dict = row.asDict()
            for col in all_columns:
                combined_data[col].append(row_dict.get(col, None))

    # 将所有批次数据合并为一个 Arrow 表
    arrow_table = pa.Table.from_pydict(combined_data)
    logger.info(f"Combined data into Arrow Table with schema: {arrow_table.schema}")

    # 构造文件名
    unique_id = uuid.uuid4().hex
    object_name = f"{config.filename}_partition_{unique_id}.arrow"
    logger.info(f"Constructed object name: {object_name}")

    # 初始化 ArrowUploader
    arrow_uploader = ArrowUploader(config.endpoint, config.accesskey, config.secretkey, minio_bucket)
    logger.info(f"Initialized ArrowUploader with endpoint: {config.endpoint}, bucket: {minio_bucket}")

    # 上传到 MinIO
    arrow_uploader.save_to_minio(arrow_table, object_name)
    logger.info(f"Uploaded partition to MinIO: {object_name}")

    # 生成预签名 URL
    presigned_url = arrow_uploader.generate_presigned_url(object_name, expires=3600)  # 1小时有效期
    if presigned_url:
        logger.info(f"Generated presigned URL for partition: {presigned_url}")
    else:
        logger.error(f"Failed to generate presigned URL for partition: {object_name}")
        return iter([])

    # 返回 URL
    return iter([presigned_url])

def notify_server_of_completion(config, partition_urls):
    server_endpoint = f"{config.serverip}:{config.serverport}"
    url = urljoin(f"http://{server_endpoint}", "/api/job/completed")
    logger.info(f"Partition URLs to notify server: {partition_urls}")
    try:
        response = requests.post(url, json={"filePaths": partition_urls}, headers={'Content-Type': 'application/json'})
        logger.info(f"Server notification response: {response.text}")
    except Exception as e:
        logger.error(f"Failed to notify server: {e}")


def convert_dataframe_to_arrow(df):
    """将 DataFrame 转换为 PyArrow Table。"""
    # 获取列名和数据
    columns = df.columns
    data = df.collect()

    # 创建 PyArrow Table
    arrow_table = pa.Table.from_arrays(
        [pa.array([row[col] for row in data]) for col in columns],
        names=columns
    )

    return arrow_table


def convert_partition_to_arrow(rows):
    """将每个分区的数据直接转换为 PyArrow Table。"""
    rows = list(rows)  # 将迭代器转为列表，避免重复消费

    if not rows:
        return  # 空分区直接跳过

    # 获取列名和数据
    columns = rows[0].__fields__ if hasattr(rows[0], "__fields__") else rows[0].asDict().keys()
    data = [[row[col] for col in columns] for row in rows]

    # 创建 PyArrow Table
    arrow_table = pa.Table.from_arrays(
        [pa.array([row[i] for row in data]) for i in range(len(columns))],
        names=columns
    )

    yield arrow_table

def calculate_optimal_partitions(spark, total_data_size_gb, ideal_partition_size_mb=128):
    """计算最优分区数"""
    total_cores = spark.sparkContext.defaultParallelism
    # 基于 CPU 核心数确定分区数量
    partitions_based_on_cores = total_cores * 1.5

    # 基于数据量确定分区数量
    partitions_based_on_data_size = (total_data_size_gb * 1024) / ideal_partition_size_mb

    # 取两个值的较大者
    optimal_partitions = max(partitions_based_on_cores, partitions_based_on_data_size)
    return int(optimal_partitions)

if __name__ == "__main__":
    config = parse_args()
    run_spark_job(config)
