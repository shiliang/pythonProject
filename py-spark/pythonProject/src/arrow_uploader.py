from minio import Minio, S3Error
import pyarrow.ipc as ipc
import io


class ArrowUploader:

    def __init__(self, endpoint, access_key, secret_key, bucket_name):
        print(f"Endpoint: {endpoint}")
        print(f"Access Key: {access_key}")
        print(f"Secret Key: {secret_key}")
        print(f"Bucket Name: {bucket_name}")
        self.minio_client = Minio(
            endpoint=endpoint,
            access_key=access_key,
            secret_key=secret_key,
            secure=False
        )
        self.bucket_name = bucket_name

    def save_to_minio(self, arrow_table, object_name):
        byte_stream = io.BytesIO()

        # 使用 PyArrow IPC 将数据写入 ByteArrayOutputStream
        with ipc.new_file(byte_stream, arrow_table.schema) as writer:
            writer.write_table(arrow_table)

        byte_stream.seek(0)  # 重置流

        # 上传文件到 MinIO，打印内容验证流数据
        print(f"Stream data (first 256 bytes): {byte_stream.getvalue()[:256]}")

        # 上传到 MinIO
        self.minio_client.put_object(
            bucket_name=self.bucket_name,
            object_name=object_name,
            data=byte_stream,
            length=len(byte_stream.getvalue()),
            content_type='application/octet-stream'
        )
        print(f"Uploaded {object_name} successfully to MinIO.")

    def generate_presigned_url(self, object_name, expires=3600):
        """生成预签名 URL"""
        try:
            presigned_url = self.minio_client.presigned_get_object(
                self.bucket_name,
                object_name,
                expires=expires  # 默认过期时间为3600秒（1小时）
            )
            return presigned_url
        except S3Error as e:
            print(f"Error generating presigned URL: {e}")
            return None