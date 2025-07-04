package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
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
        val order = command.completedOrder

        order.products.forEach {
            val stat = ProductSaleStat.new(
                productId = it.productId,
                quantity = it.quantity,
            )
            statRepository.save(stat)
        }
    }

    @Transactional
    fun createDailyStats(command: CreateProductDailySaleStatsCommand) {
        dailyStatRepository.aggregateDailyStatsByDate(command.date)
    }
}
