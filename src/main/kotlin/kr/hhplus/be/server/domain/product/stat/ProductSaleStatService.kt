package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.common.cache.CacheNames
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductSaleStatService(
    private val statRepository: ProductSaleStatRepository,
    private val dailyStatRepository: ProductDailySaleStatRepository,
) {
    @Transactional
    fun createStats(command: CreateProductSaleStatsCommand) {
        val order = command.event.snapshot

        order.orderProducts.forEach {
            val stat = ProductSaleStat.new(
                productId = it.productId,
                quantity = it.quantity,
            )
            statRepository.save(stat)
        }
    }

    @Transactional
    @CacheEvict(CacheNames.POPULAR_PRODUCTS, key =  "'popular_products'")
    fun createDailyStats(command: CreateProductDailySaleStatsCommand) {
        dailyStatRepository.aggregateDailyStatsByDate(command.date)
    }

    @Cacheable(CacheNames.POPULAR_PRODUCTS, key = "'popular_products'", unless = "#result.value.isEmpty()")
    fun getPopularProductIds(): PopularProductsIds =
        dailyStatRepository.findTopNProductsByQuantity(
            startDate = PopularProductsIds.getStartDay(),
            endDate = PopularProductsIds.getEndDay(),
            limit = PopularProductsIds.MAX_SIZE,
        ).map { it.productId }.let { PopularProductsIds(it) }
}
