package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingService
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductRankingEventListener(
    private val productSaleRankingService: ProductSaleRankingService,
) {
    @Async
    @TransactionalEventListener
    fun handle(event: OrderCompletedEvent) {
        productSaleRankingService.updateRanking(UpdateProductSaleRankingCommand(event))
    }
}
