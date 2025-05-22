package kr.hhplus.be.server.interfaces.order.event.order

import kr.hhplus.be.server.application.order.OrderCancelFacade
import kr.hhplus.be.server.application.order.OrderSendFacade
import kr.hhplus.be.server.application.order.command.CancelOrderFacadeCommand
import kr.hhplus.be.server.application.order.command.SendCompletedOrderFacadeCommand
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val orderCancelFacade: OrderCancelFacade,
    private val orderSendFacade: OrderSendFacade
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OrderFailedEvent) {
        logger.info("OrderFailedEvent 수신: orderId={}", event.orderId.value)
        runCatching {
            orderCancelFacade.cancel(CancelOrderFacadeCommand(event.order))
        }.onSuccess {
            logger.info("주문 취소 처리 완료: orderId={}", event.orderId.value)
        }.onFailure { exception ->
            logger.error("주문 취소 처리 중 오류 발생: orderId={}, error={}", event.orderId.value, exception.message)
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OrderCompletedEvent) {
        logger.info("OrderCompletedEvent 수신: orderId={}", event.orderId.value)
        runCatching {
            orderSendFacade.sendCompleted(SendCompletedOrderFacadeCommand(event.order))
        }.onSuccess {
            logger.info("주문 완료 데이터 전송 완료: orderId={}", event.orderId.value)
        }.onFailure { exception ->
            logger.error("주문 완료 데이터 전송 중 오류 발생: orderId={}, error={}", event.orderId.value, exception.message)
        }
    }
}
