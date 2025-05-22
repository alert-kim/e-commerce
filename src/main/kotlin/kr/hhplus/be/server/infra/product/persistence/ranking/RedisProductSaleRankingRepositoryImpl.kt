package kr.hhplus.be.server.infra.product.persistence.ranking

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDate

@Repository
class RedisProductSaleRankingRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, String>,
) : ProductSaleRankingRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val ops = redisTemplate.opsForZSet()

    override fun updateRanking(entry: ProductSaleRankingEntry) {
        val key = ProductRankingRedisKeyGenerator.dailyKey(entry.date)
        ops.incrementScore(
            key,
            entry.productId.value.toString(),
            RedisProductSaleScoreCalculator.calculate(entry),
        )
        redisTemplate.expire(key, DAILY_KEY_TTL)
    }

    override fun renewRanking(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int,
    ): List<ProductId> {
        logger.info("Renewing ranking for date range: {} to {}, limit: {}", startDate, endDate, limit)

        val keys = validateAndBuildDailyKeys(startDate, endDate)
        val unionKey = ProductRankingRedisKeyGenerator.unionKey(start = startDate, end = endDate)

        val productCountInRanking = mergeDailyRankings(keys, unionKey)
        trimRankingToLimit(unionKey, productCountInRanking, limit)
        redisTemplate.expire(unionKey, UNION_KEY_TTL)

        return fetchTopProductIds(unionKey, limit)
    }

    override fun findTopNProductIds(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<ProductId> {
        val unionKey = ProductRankingRedisKeyGenerator.unionKey(startDate, endDate)
        return fetchTopProductIds(unionKey, limit)
    }

    private fun validateAndBuildDailyKeys(start: LocalDate, end: LocalDate): List<String> {
        val keys = start.rangeToInclusive(end).toKeys()
        require(keys.isNotEmpty()) { "Invalid dates to find product ranking ($start - $end)" }
        return keys
    }

    private fun mergeDailyRankings(dailyKeys: List<String>, unionKey: String): Long {
        val count = ops.unionAndStore(dailyKeys.first(), dailyKeys.drop(1), unionKey)
        if (count == null || count <= 0) {
            logger.warn("No ranking entry found for date range: {}", unionKey)
            return 0
        }
        return count
    }

    private fun trimRankingToLimit(unionKey: String, count: Long, limit: Int) {
        if (count > limit) {
            ops.removeRange(unionKey, limit.toLong() -1, 0)
        }
    }

    private fun fetchTopProductIds(unionKey: String, limit: Int): List<ProductId> {
        val result = ops.reverseRange(unionKey, 0, limit - 1L)
        logger.info("Found {} products in ranking for unionKey: {}", result?.size ?: 0, unionKey)
        return result?.map { ProductId(it.toLong()) } ?: emptyList()
    }

    private fun List<LocalDate>.toKeys() =
        map { ProductRankingRedisKeyGenerator.dailyKey(it) }

    private fun LocalDate.rangeToInclusive(endDate: LocalDate): List<LocalDate> =
        generateSequence(this) { it.plusDays(1) }
            .takeWhile { it <= endDate }
            .toList()

    companion object {
        private val DAILY_KEY_TTL = Duration.ofDays(4)
        private val UNION_KEY_TTL = Duration.ofDays(2)
    }
}
