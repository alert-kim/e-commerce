package kr.hhplus.be.server.application.order

import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.hhplus.be.server.application.order.command.CancelCouponUseProcessorCommand
import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.MarkOrderFailHandledProcessorCommand
import kr.hhplus.be.server.application.order.command.RestoreStockOrderProductProcessorCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderSnapshot
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class OrderCancelEventListenerTest {

    private lateinit var orderCancelEventListener: OrderCancelEventListener

    private val orderService = mockk<OrderService>(relaxed = true)
    private val orderCouponProcessor = mockk<OrderCouponProcessor>(relaxed = true)
    private val orderProductProcessor = mockk<OrderProductProcessor>(relaxed = true)
    private val orderPaymentProcessor = mockk<OrderPaymentProcessor>(relaxed = true)
    private val orderLifecycleProcessor = mockk<OrderLifecycleProcessor>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        orderCancelEventListener = OrderCancelEventListener(
            orderCouponProcessor = orderCouponProcessor,
            orderProductProcessor = orderProductProcessor,
            orderPaymentProcessor = orderPaymentProcessor,
            orderLifecycleProcessor = orderLifecycleProcessor,
            orderService = orderService,
        )
    }

    @Nested
    @DisplayName("주문 실패 이벤트 처리")
    inner class HandleOrderFailedEvent {

        @Test
        @DisplayName("FAILED 상태가 아닌 주문은 취소 처리하지 않는다")
        fun notFailed() {
            val order = OrderMock.view(status = OrderStatus.FAIL_HANDLED)
            val event = OrderMock.failedEvent(orderId = order.id)
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verify(exactly = 0) {
                orderCouponProcessor.cancelCoupon(any())
                orderProductProcessor.restoreOrderProductStock(any())
                orderPaymentProcessor.cancelPayment(any())
                orderLifecycleProcessor.markFailHandled(any())
            }
        }

        @Test
        @DisplayName("모든 취소 처리를 진행하고, 문제가 없다면 주문을 실패 처리 완료한다")
        fun handleAllAndMarkSucceeds() {
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = CouponMock.id())
            val event = OrderMock.failedEvent(orderId = order.id)
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verify {
                orderCouponProcessor.cancelCoupon(ofType(CancelCouponUseProcessorCommand::class))
                orderProductProcessor.restoreOrderProductStock(ofType(RestoreStockOrderProductProcessorCommand::class))
                orderPaymentProcessor.cancelPayment(ofType(CancelOrderPaymentProcessorCommand::class))
                orderLifecycleProcessor.markFailHandled(ofType(MarkOrderFailHandledProcessorCommand::class))
            }
        }

        @Test
        @DisplayName("쿠폰 취소 처리")
        fun cancelCoupon() {
            val couponId = CouponMock.id()
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = couponId)
            val event = OrderMock.failedEvent(
                orderId = order.id,
                snapshot = OrderSnapshot.from(OrderMock.order(couponId = couponId))
            )
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verify {
                orderCouponProcessor.cancelCoupon(CancelCouponUseProcessorCommand(couponId))
            }
        }

        @Test
        @DisplayName("쿠폰 취소 처리가 실패해도 다른 취소 작업은 진행하고, 주문 실패 처리 완료는 진행하지 않는다")
        fun handleOtherIfCancelCouponFail() {
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = CouponMock.id())
            val event = OrderMock.failedEvent(orderId = order.id)
            every { orderService.get(order.id.value) } returns order
            every { orderCouponProcessor.cancelCoupon(any()) } throws RuntimeException()

            orderCancelEventListener.handle(event)

            verify {
                orderProductProcessor.restoreOrderProductStock(ofType(RestoreStockOrderProductProcessorCommand::class))
                orderPaymentProcessor.cancelPayment(ofType(CancelOrderPaymentProcessorCommand::class))
            }
            verify(exactly = 0) {
                orderLifecycleProcessor.markFailHandled(ofType(MarkOrderFailHandledProcessorCommand::class))
            }
        }

        @Test
        @DisplayName("주문 상품 재고 원복 처리")
        fun restoreStock() {
            val orderProducts = List(2) { OrderMock.product() }
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val event = OrderMock.failedEvent(
                orderId = order.id,
                snapshot = OrderSnapshot.from(OrderMock.order(products = orderProducts))
            )
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verify {
                orderProducts.forEach { product ->
                    orderProductProcessor.restoreOrderProductStock(
                        RestoreStockOrderProductProcessorCommand(
                            productId = product.productId,
                            quantity = product.quantity
                        )
                    )
                }
            }
        }

        @Test
        @DisplayName("주문 상품 재고 원복 처리는 productId 기준으로 정렬하여 처리")
        fun restoreStockInOrder() {
            val orderProducts = listOf(
                OrderMock.product(productId = ProductId(3L)),
                OrderMock.product(productId = ProductId(1L)),
                OrderMock.product(productId = ProductId(2L)),
            )
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val event = OrderMock.failedEvent(
                orderId = order.id,
                snapshot = OrderSnapshot.from(OrderMock.order(products = orderProducts))
            )
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verifyOrder {
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                  it.productId.value shouldBe 1L
                })
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    it.productId.value shouldBe 2L
                })
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    it.productId.value shouldBe 3L
                })
            }
        }

        @Test
        @DisplayName("주문 상품 재고 원복 처리가 일부 실패해도, 다른 상품은 원복 처리한다")
        fun handleOtherIfSomeRestoreAllocationFail() {
            val restoreFailProduct = OrderMock.product()
            val restoreSuccessProduct = OrderMock.product()
            val orderProducts = listOf(restoreFailProduct, restoreSuccessProduct)
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val event = OrderMock.failedEvent(
                orderId = order.id,
                snapshot = OrderSnapshot.from(OrderMock.order(products = orderProducts))
            )
            every { orderService.get(order.id.value) } returns order
            every {
                orderProductProcessor.restoreOrderProductStock(
                    RestoreStockOrderProductProcessorCommand(
                        restoreFailProduct.productId,
                        restoreFailProduct.quantity
                    )
                )
            } throws RuntimeException()

            orderCancelEventListener.handle(event)

            verify {
                orderProductProcessor.restoreOrderProductStock(
                    RestoreStockOrderProductProcessorCommand(
                        productId = restoreSuccessProduct.productId,
                        quantity = restoreSuccessProduct.quantity
                    )
                )
            }
        }

        @Test
        @DisplayName("주문 상품 재고 원복 처리가 실패해도 다른 취소 작업은 진행하고, 주문 실패 처리 완료는 진행하지 않는다")
        fun handleOtherIfCancelStockAllocationFail() {
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = CouponMock.id())
            val event = OrderMock.failedEvent(orderId = order.id)
            every { orderService.get(order.id.value) } returns order
            every { orderProductProcessor.restoreOrderProductStock(any()) } throws RuntimeException()

            orderCancelEventListener.handle(event)

            verify {
                orderCouponProcessor.cancelCoupon(ofType(CancelCouponUseProcessorCommand::class))
                orderPaymentProcessor.cancelPayment(ofType(CancelOrderPaymentProcessorCommand::class))
            }
            verify(exactly = 0) {
                orderLifecycleProcessor.markFailHandled(ofType(MarkOrderFailHandledProcessorCommand::class))
            }
        }

        @Test
        @DisplayName("결제 취소 처리")
        fun cancelPayment() {
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val event =
                OrderMock.failedEvent(orderId = order.id, snapshot = OrderSnapshot.from(OrderMock.order(id = order.id, userId = order.userId)))
            every { orderService.get(order.id.value) } returns order

            orderCancelEventListener.handle(event)

            verify {
                orderPaymentProcessor.cancelPayment(
                    CancelOrderPaymentProcessorCommand(
                        orderId = order.id,
                        userId = order.userId,
                    )
                )
            }
        }

        @Test
        @DisplayName("결제 취소 처리가 다른 취소 작업은 진행하고, 주문 실패 처리 완료는 진행하지 않는다")
        fun handleOtherIfCancelPaymentFail() {
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = CouponMock.id())
            val event = OrderMock.failedEvent(orderId = order.id)
            every { orderService.get(order.id.value) } returns order
            every { orderPaymentProcessor.cancelPayment(any()) } throws RuntimeException()

            orderCancelEventListener.handle(event)

            verify {
                orderCouponProcessor.cancelCoupon(ofType(CancelCouponUseProcessorCommand::class))
                orderProductProcessor.restoreOrderProductStock(ofType(RestoreStockOrderProductProcessorCommand::class))
            }
            verify(exactly = 0) {
                orderLifecycleProcessor.markFailHandled(ofType(MarkOrderFailHandledProcessorCommand::class))
            }
        }

    }
}
