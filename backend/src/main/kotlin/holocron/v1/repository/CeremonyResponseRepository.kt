package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import holocron.v1.CeremonyResponse
import org.bson.Document
import org.bson.types.Binary
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.map

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

    suspend fun findByTemplateId(
        templateId: String,
        startDate: com.google.protobuf.Timestamp? = null,
        endDate: com.google.protobuf.Timestamp? = null
    ): List<CeremonyResponse> {
        val filters = mutableListOf<org.bson.conversions.Bson>(eq("templateId", templateId))
        
        if (startDate != null && startDate.seconds > 0) {
            filters.add(com.mongodb.client.model.Filters.gte("submittedAt", startDate.seconds))
        }
        if (endDate != null && endDate.seconds > 0) {
            filters.add(com.mongodb.client.model.Filters.lte("submittedAt", endDate.seconds))
        }

        return collection.find(com.mongodb.client.model.Filters.and(filters)).toList().map { doc ->
            val payload = doc.get("payload", Binary::class.java)
            CeremonyResponse.parseFrom(payload.data)
        }
    }
}
