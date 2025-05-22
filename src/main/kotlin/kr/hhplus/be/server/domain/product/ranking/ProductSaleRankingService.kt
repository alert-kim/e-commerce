package kr.hhplus.be.server.domain.product.ranking

import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import kr.hhplus.be.server.domain.product.ranking.repository.RenewProductSaleRankingCommand
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import kr.hhplus.be.server.domain.product.stat.PopularProductsIds
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ProductSaleRankingService(
    private val repository: ProductSaleRankingRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun updateRanking(command: UpdateProductSaleRankingCommand) {
        val order = command.completedOrder
        val orderCompletedAt = order.getOrNullCompletedAt() ?: return
        val date = LocalDate.ofInstant(orderCompletedAt, TimeZone.KSTId)
        order.products.forEach {
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = date,
                    productId = it.productId,
                    quantity = it.quantity,
                    orderCount = 1,
                )
            )
        }
    }

    fun renewRanking(command: RenewProductSaleRankingCommand): PopularProductsIds {
        val baseDate = command.date
        val popularProductIds = repository.renewRanking(
            startDate = PopularProductsIds.getStartDateFromBaseDate(baseDate),
            endDate = baseDate,
            limit = PopularProductsIds.MAX_SIZE,
        ).let { PopularProductsIds(it) }
        logger.info("[ProductSaleRanking] renewed popular product ids for ${command.date}: $popularProductIds")
        return popularProductIds
    }

    fun getPopularProductIds(): PopularProductsIds {
        val baseDate = LocalDate.now(TimeZone.KSTId)
        val todayPopularProductsIds = repository.findTopNProductIds(
            startDate = PopularProductsIds.getStartDateFromBaseDate(baseDate),
            endDate = baseDate,
            limit = PopularProductsIds.MAX_SIZE,
        )
        val popularProductsIds = when (todayPopularProductsIds.isEmpty()) {
            true -> repository.findTopNProductIds(
                startDate = PopularProductsIds.getStartDateFromBaseDate(baseDate.minusDays(1)),
                endDate = baseDate.minusDays(1),
                limit = PopularProductsIds.MAX_SIZE,
            )

            false -> todayPopularProductsIds
        }.let { PopularProductsIds(it) }
        return popularProductsIds
    }
}

