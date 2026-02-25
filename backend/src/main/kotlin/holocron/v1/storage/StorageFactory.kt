package holocron.v1.storage

object StorageFactory {
    fun createActiveProvider(): FileStorageProvider {
        val driver = System.getenv("STORAGE_DRIVER")?.lowercase() ?: "memory"
        
        println("🔧 Initializing Storage Provider with driver: $driver")
        
        return when (driver) {
            "local" -> {
                val path = System.getenv("STORAGE_PATH") ?: "/tmp/holocron-assets"
                LocalStorageProvider(path)
            }
            "s3" -> {
                val bucket = System.getenv("S3_BUCKET") ?: error("S3_BUCKET environment variable is required when using the 's3' driver")
                val endpoint = System.getenv("S3_ENDPOINT")
                S3StorageProvider(bucket, endpoint)
            }
            "memory" -> InMemoryStorageProvider()
            else -> {
                println("⚠️ Unknown storage driver '$driver', falling back to 'memory'")
                InMemoryStorageProvider()
            }
        }
    }
}
