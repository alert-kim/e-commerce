package kr.hhplus.be.server.scheduler.order

import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.ConsumeOrderEventFacadeCommand
import kr.hhplus.be.server.application.order.command.SendOrderFacadeCommand
import kr.hhplus.be.server.domain.order.event.OrderEventType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OrderCompletionSendingScheduler(
    private val orderFacade: OrderFacade,
) {
    @Scheduled(fixedRate = 10_000)
    fun send() {
        val events = orderFacade.getAllEventsNotHandledInOrder(SCHEDULER_ID, OrderEventType.COMPLETED)
        events.forEach {
            orderFacade.sendOrderCompletionData(
                command = SendOrderFacadeCommand(it.snapshot)
            )
            orderFacade.consumeEvent(
                ConsumeOrderEventFacadeCommand(
                    consumerId = SCHEDULER_ID,
                    event = it
                )
            )
        }
    }


    companion object {
        val eventType = OrderEventType.COMPLETED
        const val SCHEDULER_ID = "order-completion-sending"
    }
}
