package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSaleStatService(
    private val statRepository: ProductSaleStatRepository,
    private val dailyStatRepository: ProductDailySaleRepository,
) {
    @Transactional
    fun createStats(command: CreateProductSaleStatsCommand) {
        val order = command.event.snapshot

        order.orderProducts.forEach {
            val stat = ProductSaleStat.new(
                productId = ProductId(it.productId),
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
