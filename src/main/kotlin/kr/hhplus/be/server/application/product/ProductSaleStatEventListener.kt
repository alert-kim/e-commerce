package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductSaleStatEventListener(
    private val productStatService: ProductSaleStatService,
) {
    @Async
    @TransactionalEventListener
    fun handle(event: OrderCompletedEvent) {
        productStatService.createStats(CreateProductSaleStatsCommand(event))
    }
}
