package holocron.v1.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

class CacheFactoryTest {

    @Test
    fun `test default cache factory creates local cache`() {
        // Technically this relies on CACHE_TYPE not being "distributed"
        val cache = CacheFactory.createCache<String, String>()
        assertTrue(cache is CaffeineCacheAdapter)
    }

    // We skip RedisCacheAdapter tests as we don't have a reliable redis instance
    // running in the test environment for standard gradlew test.
    // Real end-to-end integration tests could use testcontainers later.
}
