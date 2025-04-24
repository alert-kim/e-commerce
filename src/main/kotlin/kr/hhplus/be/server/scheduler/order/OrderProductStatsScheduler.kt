package kr.hhplus.be.server.scheduler.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.ConsumeOrderEventsFacadeCommand
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.domain.order.event.OrderEventType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrderProductStatsScheduler(
    private val orderFacade: OrderFacade,
    private val productFacade: ProductFacade,
) {
    @Scheduled(fixedRate = 10_000)
    fun aggregate() {
        val events = orderFacade.getAllEventsNotConsumedInOrder(SCHEDULER_ID, eventType).value
        val orderProducts = events.flatMap { it.snapshot.orderProducts }

        productFacade.aggregate(
            AggregateProductDailySalesFacadeCommand(
               orderProducts
            )
        )

        orderFacade.consumeEvent(
            ConsumeOrderEventsFacadeCommand(
                consumerId = SCHEDULER_ID,
                events = events
            )
        )
    }

    companion object {
        val eventType = OrderEventType.COMPLETED
        const val SCHEDULER_ID = "order-product-stats"
    }
}
