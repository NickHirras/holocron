package holocron.v1.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

class CaffeineCacheAdapterTest {

    @Test
    fun `test put and get`() {
        val cache = CaffeineCacheAdapter<String, String>()
        
        cache.put("key1", "value1", Duration.ofMinutes(5))
        
        assertEquals("value1", cache.get("key1"))
        assertNull(cache.get("key2"))
    }

    @Test
    fun `test eviction`() {
        val cache = CaffeineCacheAdapter<String, String>()
        
        cache.put("key1", "value1", Duration.ofMinutes(5))
        assertEquals("value1", cache.get("key1"))
        
        cache.evict("key1")
        assertNull(cache.get("key1"))
    }

    @Test
    fun `test expiration`() {
        val cache = CaffeineCacheAdapter<String, String>()
        
        // Use a tiny TTL
        cache.put("key1", "value1", Duration.ofMillis(10))
        
        assertEquals("value1", cache.get("key1"))
        
        // Wait for expiration
        Thread.sleep(50)
        
        assertNull(cache.get("key1"))
    }
}
