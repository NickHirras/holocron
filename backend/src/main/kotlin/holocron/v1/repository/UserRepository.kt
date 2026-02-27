package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import holocron.v1.User
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import io.viascom.nanoid.NanoId

import org.bson.Document

class UserRepository(private val mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val collection: MongoCollection<Document> = database.getCollection("users")

    suspend fun findOrCreate(email: String): User {
        val existingDoc = collection.find(eq("email", email)).firstOrNull()
        if (existingDoc != null) {
            val blob = existingDoc.get("blob", org.bson.types.Binary::class.java).data
            return User.parseFrom(blob)
        }

        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
            
        val newUser = User.newBuilder()
            .setId(NanoId.generate(12, "23456789abcdefghjkmnpqrstuvwxyz"))
            .setEmail(email)
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .build()

        val doc = Document("_id", newUser.id)
            .append("email", newUser.email)
            .append("blob", newUser.toByteArray())

        collection.insertOne(doc)
        return newUser
    }

    suspend fun getUsers(userEmails: List<String>): List<User> {
        if (userEmails.isEmpty()) return emptyList()
        return collection.find(com.mongodb.client.model.Filters.`in`("email", userEmails))
            .toList()
            .mapNotNull { doc ->
                val blob = doc.get("blob", org.bson.types.Binary::class.java)?.data ?: return@mapNotNull null
                User.parseFrom(blob)
            }
    }
}
