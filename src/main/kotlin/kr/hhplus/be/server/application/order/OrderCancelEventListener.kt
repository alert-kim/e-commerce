package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.MarkOrderFailHandledProcessorCommand
import kr.hhplus.be.server.application.order.command.RestoreStockOrderProductProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderCancelEventListener(
    private val orderService: OrderService,
    private val orderCouponProcessor: OrderCouponProcessor,
    private val orderProductProcessor: OrderProductProcessor,
    private val orderPaymentProcessor: OrderPaymentProcessor,
    private val orderLifecycleProcessor: OrderLifecycleProcessor
) {
    private val logger = LoggerFactory.getLogger(OrderCancelEventListener::class.java)

    @TransactionalEventListener
    fun handle(event: OrderFailedEvent) {
        val order = orderService.get(event.orderId.value)

        if (order.isFailed().not()) {
            logger.warn(
                "[OrderCancelEventListener] order status is not FAILED: order={}, status={}",
                order.id.value,
                order.status,
            )
            return
        }

        logger.info("주문 취소 처리 시작: orderId={}", order.id)
        val snapshot = event.snapshot
        val isSuccess = processCancellation(snapshot)

        if (isSuccess) {
            orderLifecycleProcessor.markFailHandled(
                MarkOrderFailHandledProcessorCommand(orderId = snapshot.id)
            )
        }
    }

    private fun processCancellation(
        snapshot: kr.hhplus.be.server.domain.order.OrderSnapshot
    ): Boolean {
        var isSuccess = true

        isSuccess = cancelCouponIfPresent(snapshot) && isSuccess
        isSuccess = cancelOrderProducts(snapshot) && isSuccess
        isSuccess = cancelPayment(snapshot) && isSuccess

        return isSuccess
    }

    private fun cancelCouponIfPresent(
        snapshot: kr.hhplus.be.server.domain.order.OrderSnapshot
    ): Boolean {
        val couponId = snapshot.couponId ?: return true

        return runCatching {
            orderCouponProcessor.cancelCoupon(CancelCouponUseProcessorCommand(couponId))
            true
        }.getOrElse { error ->
            logger.error(
                "[OrderCancelEventListener] fail to cancel coupon: order={}, couponId={}",
                snapshot.id, couponId, error
            )
            false
        }
    }

    private fun cancelOrderProducts(
        snapshot: kr.hhplus.be.server.domain.order.OrderSnapshot
    ): Boolean {
        var isAllSuccess = true
        snapshot.orderProducts
            .sortedBy { it.productId.value }
            .forEach { orderProduct ->
            runCatching {
                orderProductProcessor.restoreOrderProductStock(
                    RestoreStockOrderProductProcessorCommand(
                        productId = orderProduct.productId,
                        quantity = orderProduct.quantity,
                    )
                )
            }.getOrElse { error ->
                logger.error(
                    "[OrderCancelEventListener] fail to restore product stock: order={}, product={}",
                    snapshot.id.value,
                    orderProduct.productId,
                    error
                )
                isAllSuccess = false
            }
        }
        return isAllSuccess
    }

    private fun cancelPayment(
        snapshot: kr.hhplus.be.server.domain.order.OrderSnapshot
    ): Boolean =
        runCatching {
            orderPaymentProcessor.cancelPayment(
                CancelOrderPaymentProcessorCommand(
                    orderId = snapshot.id,
                    userId = snapshot.userId,
                )
            )
            true
        }.getOrElse { error ->
            logger.error("[OrderCancelEventListener] fail to cancel order: order={}", snapshot.id.value, error)
            false
        }
}
