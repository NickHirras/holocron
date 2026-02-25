package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.Document
import org.bson.types.Binary
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ImageRepository(mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val collection = database.getCollection<Document>("images")

    suspend fun save(bytes: ByteArray, contentType: String): String {
        val id = UUID.randomUUID().toString()
        val doc = Document("_id", id)
            .append("contentType", contentType)
            .append("data", Binary(bytes))

        collection.insertOne(doc)
        return id
    }

    suspend fun findById(id: String): Pair<ByteArray, String>? {
        val doc = collection.find(eq("_id", id)).firstOrNull() ?: return null
        val data = doc.get("data", Binary::class.java) ?: return null
        val contentType = doc.getString("contentType") ?: "application/octet-stream"
        return Pair(data.data, contentType)
    }
}
