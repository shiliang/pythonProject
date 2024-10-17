import argparse
import logging
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
    spark = SparkSession.builder.appName("Dynamic Database Job").getOrCreate()
    strategy = DatabaseFactory.get_strategy(
        config.dbtype, config.host, config.port, config.database, config.username, config.password
    )
    minio_bucket = "batch-data"
    local_file_path = f"/tmp/arrow_data/{config.filename}.arrow"
    output_path = f"s3a://{minio_bucket}/{config.filename}.arrow"

    logger.info(f"Generated filename: {config.filename}")
    logger.info(f"Generated query: {config.query}")

    try:
        df = spark.read \
            .format("jdbc") \
            .option("url", strategy.get_jdbc_url()) \
            .option("dbtable", f"({config.query.strip(';')}) dbtable") \
            .option("driver", strategy.get_driver()) \
            .load()
        print("DataFrame head:\n", df.head())
        arrow_uploader = ArrowUploader(config.endpoint, config.accesskey, config.secretkey, minio_bucket)
        arrow_batches = df.rdd.mapPartitions(convert_partition_to_arrow)

        logger.info("DataFrame written to MinIO successfully.")
    except Exception as e:
        logger.error(f"Error writing DataFrame to MinIO: {e}")

    for i, arrow_table in enumerate(arrow_batches.collect()):
        arrow_uploader.save_to_minio(arrow_table, f"arrow_data_part_{i}.arrow")
    notify_server_of_completion(config, output_path)
    spark.stop()

def notify_server_of_completion(config, minio_url):
    server_endpoint = f"{config.serverip}:{config.serverport}"
    url = urljoin(f"http://{server_endpoint}", "/api/job/completed")

    try:
        response = requests.post(url, json={"filePath": minio_url})
        logger.info(f"Server notification response: {response.text}")
    except Exception as e:
        logger.error(f"Failed to notify server: {e}")

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

if __name__ == "__main__":
    config = parse_args()
    run_spark_job(config)
