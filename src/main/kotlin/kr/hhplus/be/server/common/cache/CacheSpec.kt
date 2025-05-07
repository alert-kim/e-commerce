package kr.hhplus.be.server.common.cache

import java.time.Duration

enum class CacheSpec(
    val cacheName: String,
    val ttl: Duration,
) {
    POPULAR_PRODUCTS(CacheNames.POPULAR_PRODUCTS, Duration.ofHours(25)),
}
