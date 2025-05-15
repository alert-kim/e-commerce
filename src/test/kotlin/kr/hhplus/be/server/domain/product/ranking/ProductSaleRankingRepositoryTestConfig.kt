package kr.hhplus.be.server.domain.product.ranking

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.infra.product.ranking.ProductRankingRedisKeyGenerator.dailyKey
import kr.hhplus.be.server.infra.product.ranking.ProductRankingRedisKeyGenerator.unionKey
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.RedisTemplate
import java.time.LocalDate

@TestConfiguration
class ProductSaleRankingRepositoryTestConfig {

    @Bean
    fun testProductSaleRankingRepository(redisTemplate: RedisTemplate<String, String>): TestProductSaleRankingRedisRepository =
        TestProductSaleRankingRedisRepository(redisTemplate)
}

class TestProductSaleRankingRedisRepository(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun deleteAll() {
        redisTemplate.keys("*").forEach { key ->
            redisTemplate.delete(key)
        }
    }
}
