package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import holocron.v1.CeremonyResponse
import org.bson.Document
import org.bson.types.Binary

class CeremonyResponseRepository(mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val collection = database.getCollection<Document>("ceremony_responses")

    suspend fun save(response: CeremonyResponse) {
        val payload = Binary(response.toByteArray())
        val doc = Document("_id", response.responseId)
            .append("templateId", response.ceremonyTemplateId)
            .append("userId", response.userId)
            .append("submittedAt", response.submittedAt.seconds)
            .append("payload", payload)

        collection.replaceOne(
            eq("_id", response.responseId),
            doc,
            ReplaceOptions().upsert(true)
        )
    }
}
