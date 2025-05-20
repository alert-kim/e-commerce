package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.MarkOrderFailHandledProcessorCommand
import kr.hhplus.be.server.application.order.command.RestoreStockOrderProductProcessorCommand
import kr.hhplus.be.server.application.order.processor.OrderCouponProcessor
import kr.hhplus.be.server.application.order.processor.OrderLifecycleProcessor
import kr.hhplus.be.server.application.order.processor.OrderPaymentProcessor
import kr.hhplus.be.server.application.order.processor.OrderProductProcessor
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
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

    @Async
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
        val orderView = event.order
        val isSuccess = processCancellation(orderView)

        if (isSuccess) {
            orderLifecycleProcessor.markFailHandled(
                MarkOrderFailHandledProcessorCommand(orderId = orderView.id)
            )
        }
    }

    private fun processCancellation(
        order: OrderView,
    ): Boolean {
        var isSuccess = true

        isSuccess = cancelCouponIfPresent(order) && isSuccess
        isSuccess = cancelOrderProducts(order) && isSuccess
        isSuccess = cancelPayment(order) && isSuccess

        return isSuccess
    }

    private fun cancelCouponIfPresent(
        order: OrderView,
    ): Boolean {
        val couponId = order.couponId ?: return true

        return runCatching {
            orderCouponProcessor.cancelCoupon(CancelCouponUseProcessorCommand(couponId))
            true
        }.getOrElse { error ->
            logger.error(
                "[OrderCancelEventListener] fail to cancel coupon: order={}, couponId={}",
                order.id, couponId, error
            )
            false
        }
    }

    private fun cancelOrderProducts(
        order: OrderView
    ): Boolean {
        var isAllSuccess = true
        order.products
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
                    order.id.value,
                    orderProduct.productId,
                    error
                )
                isAllSuccess = false
            }
        }
        return isAllSuccess
    }

    private fun cancelPayment(
        order: OrderView,
    ): Boolean =
        runCatching {
            orderPaymentProcessor.cancelPayment(
                CancelOrderPaymentProcessorCommand(
                    orderId = order.id,
                    userId = order.userId,
                )
            )
            true
        }.getOrElse { error ->
            logger.error("[OrderCancelEventListener] fail to cancel order: order={}", order.id.value, error)
            false
        }
}
