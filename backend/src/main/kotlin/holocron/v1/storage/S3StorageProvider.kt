package holocron.v1.storage

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import aws.smithy.kotlin.runtime.net.url.Url

class S3StorageProvider(
    private val bucket: String,
    private val endpoint: String? = null
) : FileStorageProvider {

    private val client = S3Client {
        region = System.getenv("AWS_REGION") ?: "us-east-1"
        if (!endpoint.isNullOrBlank()) {
            endpointUrl = Url.parse(endpoint)
            forcePathStyle = true // Needed for MinIO/Localstack mostly
        }
    }

    override suspend fun save(filename: String, bytes: ByteArray, contentType: String): String {
        val request = PutObjectRequest {
            this.bucket = this@S3StorageProvider.bucket
            this.key = "assets/$filename"
            this.body = ByteStream.fromBytes(bytes)
            this.contentType = contentType
        }
        
        client.putObject(request)
        
        val uri = "holocron://assets/$filename"
        println("☁️ [S3Storage] Saved $filename to bucket $bucket ($uri)")
        return uri
    }

    override suspend fun get(uri: String): Pair<ByteArray, String>? {
        if (!uri.startsWith("holocron://assets/")) return null
        val filename = uri.removePrefix("holocron://assets/")
        
        val request = GetObjectRequest {
            this.bucket = this@S3StorageProvider.bucket
            this.key = "assets/$filename"
        }
        
        return try {
            client.getObject(request) { response ->
                val bytes = response.body?.toByteArray() ?: ByteArray(0)
                val contentType = response.contentType ?: "application/octet-stream"
                bytes to contentType
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            println("❌ [S3Storage] Failed to get $uri: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
