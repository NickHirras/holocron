package holocron.v1.cache

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration
import java.util.concurrent.TimeUnit
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache
import com.github.benmanes.caffeine.cache.Expiry

class CaffeineCacheAdapter<K : Any, V : Any> : CachePort<K, V> {

    // Store custom TTLs per key
    private val ttlMap = java.util.concurrent.ConcurrentHashMap<K, Long>()

    private val cache: CaffeineCache<K, V> = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfter(object : Expiry<K, V> {
            override fun expireAfterCreate(key: K, value: V, currentTime: Long): Long {
                return ttlMap[key] ?: TimeUnit.HOURS.toNanos(1)
            }
            override fun expireAfterUpdate(key: K, value: V, currentTime: Long, currentDuration: Long): Long {
                return ttlMap[key] ?: currentDuration
            }
            override fun expireAfterRead(key: K, value: V, currentTime: Long, currentDuration: Long): Long {
                return currentDuration
            }
        })
        .removalListener<K, V> { key, _, _ ->
            if (key != null) {
                ttlMap.remove(key)
            }
        }
        .build()

    override fun get(key: K): V? = cache.getIfPresent(key)

    override fun put(key: K, value: V, ttl: Duration) {
        val nanos = ttl.toNanos()
        ttlMap[key] = nanos
        cache.put(key, value)
    }

    override fun evict(key: K) {
        cache.invalidate(key)
        ttlMap.remove(key)
    }
}
