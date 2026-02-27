package holocron.v1.cache

import java.time.Duration

interface CachePort<K : Any, V : Any> {
    fun get(key: K): V?
    fun put(key: K, value: V, ttl: Duration)
    fun evict(key: K)
}
