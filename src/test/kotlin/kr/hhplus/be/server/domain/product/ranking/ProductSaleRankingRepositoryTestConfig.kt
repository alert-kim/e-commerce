package kr.hhplus.be.server.domain.product.ranking

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.RedisTemplate

@TestConfiguration
class ProductSaleRankingRepositoryTestConfig {

    @Bean
    fun testProductSaleRankingRepository(redisTemplate: RedisTemplate<String, String>): TestSaleRankingRedisRepository =
        TestSaleRankingRedisRepository(redisTemplate)
}

class TestSaleRankingRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun deleteAll() {
        redisTemplate.keys("*").forEach { key ->
            redisTemplate.delete(key)
        }
    }
}
