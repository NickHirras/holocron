package holocron.v1.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import holocron.v1.User
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class UserRepository(private val mongoClient: MongoClient) {
    private val database = mongoClient.getDatabase("holocron")
    private val collection: MongoCollection<User> = database.getCollection("users")

    suspend fun findOrCreate(email: String): User {
        val existingUser = collection.find(eq("email", email)).firstOrNull()
        if (existingUser != null) {
            return existingUser
        }

        val now = com.google.protobuf.Timestamp.newBuilder()
            .setSeconds(System.currentTimeMillis() / 1000)
            .build()
            
        val newUser = User.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setEmail(email)
            .setCreatedAt(now)
            .setUpdatedAt(now)
            .build()

        collection.insertOne(newUser)
        return newUser
    }
}
