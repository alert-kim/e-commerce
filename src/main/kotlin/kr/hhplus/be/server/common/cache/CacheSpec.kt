package kr.hhplus.be.server.common.cache

import java.time.Duration

enum class CacheSpec(
    val cacheName: String,
    val ttl: Duration,
) {
    POPULAR_PRODUCTS(CacheNames.POPULAR_PRODUCTS, Duration.ofHours(25)),
    PRODUCT(CacheNames.PRODUCT, Duration.ofHours(1)),
    STOCK_BY_PRODUCT(CacheNames.STOCK_BY_PRODUCT, Duration.ofMinutes(5)),
}
