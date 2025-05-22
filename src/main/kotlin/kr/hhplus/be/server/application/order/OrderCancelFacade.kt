package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.*
import kr.hhplus.be.server.application.order.processor.OrderCouponProcessor
import kr.hhplus.be.server.application.order.processor.OrderLifecycleProcessor
import kr.hhplus.be.server.application.order.processor.OrderPaymentProcessor
import kr.hhplus.be.server.application.order.processor.OrderProductProcessor
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.domain.order.exception.OrderCancelFailedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderCancelFacade(
    private val orderService: OrderService,
    private val orderCouponProcessor: OrderCouponProcessor,
    private val orderProductProcessor: OrderProductProcessor,
    private val orderPaymentProcessor: OrderPaymentProcessor,
    private val orderLifecycleProcessor: OrderLifecycleProcessor
) {
    private val logger = LoggerFactory.getLogger(OrderCancelFacade::class.java)

    fun cancel(command: CancelOrderFacadeCommand) {
        val currentOrder = orderService.get(command.order.id.value)

        if (currentOrder.isFailed().not()) {
            logger.warn(
                "order status is not FAILED: order={}, status={}",
                currentOrder.id.value,
                currentOrder.status,
            )
            return
        }

        val order = command.order
        logger.info("주문 취소 처리 시작: orderId={}", order.id)
        executeCancellation(currentOrder)
    }

    private fun executeCancellation(
        order: OrderView,
    ) {
        var isSuccess = true

        isSuccess = cancelCouponIfPresent(order) && isSuccess
        isSuccess = cancelOrderProducts(order) && isSuccess
        isSuccess = cancelPayment(order) && isSuccess
        if (isSuccess) {
            markOrderFailHandled(order)
        } else {
            throw OrderCancelFailedException(order.id, "주문 취소 처리에 실패했습니다.")
        }
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
                "fail to cancel coupon: order={}, couponId={}",
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
                        "fail to restore product stock: order={}, product={}",
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
            logger.error("fail to cancel payment: order={}", order.id.value, error)
            false
        }

    private fun markOrderFailHandled(
        order: OrderView,
    ) {
        orderLifecycleProcessor.markFailHandled(
            MarkOrderFailHandledProcessorCommand(orderId = order.id)
        )
    }
}
