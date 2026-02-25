package holocron.v1.storage

import java.util.concurrent.ConcurrentHashMap

class InMemoryStorageProvider : FileStorageProvider {
    private val storage = ConcurrentHashMap<String, Pair<ByteArray, String>>()

    override suspend fun save(filename: String, bytes: ByteArray, contentType: String): String {
        val uri = "holocron://assets/$filename"
        storage[uri] = bytes to contentType
        println("📝 [InMemoryStorage] Saved $filename (${bytes.size} bytes) -> $uri")
        return uri
    }

    override suspend fun get(uri: String): Pair<ByteArray, String>? {
        return storage[uri]
    }
}
