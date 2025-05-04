package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductSaleStatService(
    private val productSaleStatRepository: ProductSaleStatRepository
) {
    @Transactional
    fun createStats(command: CreateProductSaleStatsCommand) {
        val order = command.event.snapshot

        order.orderProducts.forEach {
            val stat = ProductSaleStat.new(
                productId = ProductId(it.productId),
                quantity = it.quantity,
            )
            productSaleStatRepository.save(stat)
        }
    }
}
