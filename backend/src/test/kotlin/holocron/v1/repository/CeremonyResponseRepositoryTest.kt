package holocron.v1.repository

import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.test.runTest
import org.bson.Document
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CeremonyResponseRepositoryTest {

    @Test
    fun `test hasResponded returns true when response exists`() = runTest {
        val mongoClient = mockk<MongoClient>()
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<Document>>()
        val findFlow = mockk<FindFlow<Document>>()

        every { mongoClient.getDatabase("holocron") } returns database
        every { database.getCollection<Document>("ceremony_responses") } returns collection
        every { collection.find(any<org.bson.conversions.Bson>()) } returns findFlow

        coEvery { findFlow.collect(any()) } coAnswers {
            val collector = arg<FlowCollector<Document>>(0)
            collector.emit(Document("_id", "123"))
        }

        val repository = CeremonyResponseRepository(mongoClient)

        val result = repository.hasResponded("userId", "templateId")
        assertTrue(result)
    }

    @Test
    fun `test hasResponded returns false when response does not exist`() = runTest {
        val mongoClient = mockk<MongoClient>()
        val database = mockk<MongoDatabase>()
        val collection = mockk<MongoCollection<Document>>()
        val findFlow = mockk<FindFlow<Document>>()

        every { mongoClient.getDatabase("holocron") } returns database
        every { database.getCollection<Document>("ceremony_responses") } returns collection
        every { collection.find(any<org.bson.conversions.Bson>()) } returns findFlow

        coEvery { findFlow.collect(any()) } coAnswers {
            // Emit nothing
        }

        val repository = CeremonyResponseRepository(mongoClient)

        val result = repository.hasResponded("userId", "templateId")
        assertFalse(result)
    }
}
