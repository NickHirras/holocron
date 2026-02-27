package holocron.v1.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import java.time.Duration

class RedisCacheAdapter<K : Any, V : Any>(
    redisUri: String,
    private val valueClass: Class<V>
) : CachePort<K, V> {
    private val client: RedisClient = RedisClient.create(redisUri)
    private val connection: StatefulRedisConnection<String, String> = client.connect()
    private val syncCommands = connection.sync()
    private val mapper: ObjectMapper = jacksonObjectMapper()

    override fun get(key: K): V? {
        val json = syncCommands.get(key.toString()) ?: return null
        return try {
            mapper.readValue(json, valueClass)
        } catch (e: Exception) {
            println("WARN: Failed to deserialize cache value for key: $key. Reason: ${e.message}")
            null
        }
    }

    override fun put(key: K, value: V, ttl: Duration) {
        try {
            val json = mapper.writeValueAsString(value)
            syncCommands.setex(key.toString(), ttl.seconds, json)
        } catch (e: Exception) {
            println("WARN: Failed to serialize and cache value for key: $key. Reason: ${e.message}")
        }
    }

    override fun evict(key: K) {
        syncCommands.del(key.toString())
    }
}
