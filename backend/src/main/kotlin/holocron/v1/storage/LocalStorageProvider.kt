package holocron.v1.storage

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class LocalStorageProvider(basePath: String) : FileStorageProvider {
    private val directory: Path = Paths.get(basePath)

    init {
        if (!directory.exists()) {
            directory.createDirectories()
        }
    }

    override suspend fun save(filename: String, bytes: ByteArray, contentType: String): String {
        val file = directory.resolve(filename)
        // In a real app we might want to store the content type alongside, 
        // e.g., in a metadata file or database. For LocalStorage we just guess or return octet-stream on read.
        // For simplicity, we just write the bytes.
        file.writeBytes(bytes)
        
        // Also save a .meta file so we can return the exact content type
        val metaFile = directory.resolve("$filename.meta")
        metaFile.writeBytes(contentType.toByteArray(Charsets.UTF_8))
        
        val uri = "holocron://assets/$filename"
        println("📁 [LocalStorage] Saved $filename -> $file ($uri)")
        return uri
    }

    override suspend fun get(uri: String): Pair<ByteArray, String>? {
        if (!uri.startsWith("holocron://assets/")) return null
        val filename = uri.removePrefix("holocron://assets/")
        val file = directory.resolve(filename)
        
        if (!file.exists()) return null
        
        val metaFile = directory.resolve("$filename.meta")
        val contentType = if (metaFile.exists()) {
            String(metaFile.readBytes(), Charsets.UTF_8)
        } else {
            "application/octet-stream"
        }
        
        return file.readBytes() to contentType
    }
}
