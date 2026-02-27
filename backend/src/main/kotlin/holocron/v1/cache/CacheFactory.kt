package holocron.v1.cache

object CacheFactory {
    inline fun <K : Any, reified V : Any> createCache(): CachePort<K, V> {
        val cacheType = System.getenv("CACHE_TYPE") ?: "local"
        
        return if (cacheType.equals("distributed", ignoreCase = true)) {
            val redisUri = System.getenv("REDIS_URI") ?: "redis://localhost:6379"
            RedisCacheAdapter(redisUri, V::class.java)
        } else {
            CaffeineCacheAdapter()
        }
    }
}
