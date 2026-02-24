package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoClient
import holocron.v1.CeremonyTemplate
import org.bson.Document
import org.bson.types.Binary

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList

class CeremonyTemplateRepository(mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val collection = database.getCollection<Document>("ceremony_templates")

    suspend fun save(template: CeremonyTemplate) {
        val payload = Binary(template.toByteArray())
        val doc = Document("_id", template.id)
            .append("teamId", template.teamId)
            .append("createdAt", template.createdAt.seconds)
            .append("payload", payload)

        collection.replaceOne(
            eq("_id", template.id),
            doc,
            ReplaceOptions().upsert(true)
        )
    }

    suspend fun findById(id: String): CeremonyTemplate? {
        val doc = collection.find(eq("_id", id)).firstOrNull() ?: return null
        val payload = doc.get("payload", Binary::class.java) ?: return null
        return CeremonyTemplate.parseFrom(payload.data)
    }

    suspend fun findAll(): List<CeremonyTemplate> {
        return collection.find().toList().mapNotNull { doc ->
            val payload = doc.get("payload", Binary::class.java) ?: return@mapNotNull null
            CeremonyTemplate.parseFrom(payload.data)
        }
    }
}
