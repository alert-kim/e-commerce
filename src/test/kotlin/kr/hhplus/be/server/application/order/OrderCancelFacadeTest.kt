package kr.hhplus.be.server.application.order

import io.mockk.*
import kr.hhplus.be.server.application.order.command.*
import kr.hhplus.be.server.application.order.processor.OrderCouponProcessor
import kr.hhplus.be.server.application.order.processor.OrderLifecycleProcessor
import kr.hhplus.be.server.application.order.processor.OrderPaymentProcessor
import kr.hhplus.be.server.application.order.processor.OrderProductProcessor
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.exception.OrderCancelFailedException
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class OrderCancelFacadeTest {
    private lateinit var orderCancelFacade: OrderCancelFacade
    private val orderService = mockk<OrderService>(relaxed = true)
    private val orderCouponProcessor = mockk<OrderCouponProcessor>(relaxed = true)
    private val orderProductProcessor = mockk<OrderProductProcessor>(relaxed = true)
    private val orderPaymentProcessor = mockk<OrderPaymentProcessor>(relaxed = true)
    private val orderLifecycleProcessor = mockk<OrderLifecycleProcessor>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        orderCancelFacade = OrderCancelFacade(
            orderService,
            orderCouponProcessor,
            orderProductProcessor,
            orderPaymentProcessor,
            orderLifecycleProcessor
        )
    }

    @Nested
    @DisplayName("주문 관련 취소 처리")
    inner class ProcessOrderCanceling {
        @Test
        @DisplayName("FAILED 상태가 아닌 주문은 취소 처리하지 않는다")
        fun notFailed() {
            val order = OrderMock.view(status = OrderStatus.FAIL_HANDLED)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verify(exactly = 0) {
                orderCouponProcessor.cancelCoupon(any())
                orderProductProcessor.restoreOrderProductStock(any())
                orderPaymentProcessor.cancelPayment(any())
                orderLifecycleProcessor.markFailHandled(any())
            }
        }

        @Test
        @DisplayName("모든 취소 처리를 진행하고 성공하면 예외가 발생하지 않는다")
        fun handleAllSuccessful() {
            val couponId = CouponMock.id()
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = couponId)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verify(exactly = 1) {
                orderCouponProcessor.cancelCoupon(ofType(CancelCouponUseProcessorCommand::class))
                orderProductProcessor.restoreOrderProductStock(ofType(RestoreStockOrderProductProcessorCommand::class))
                orderPaymentProcessor.cancelPayment(ofType(CancelOrderPaymentProcessorCommand::class))
                orderLifecycleProcessor.markFailHandled(ofType(MarkOrderFailHandledProcessorCommand::class))
            }
        }

        @Test
        @DisplayName("취소 처리 중 하나라도 실패하면 OrderCancelFailedException 예외가 발생한다")
        fun cancelFailed() {
            val couponId = CouponMock.id()
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = couponId)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order
            every { orderProductProcessor.restoreOrderProductStock(any()) } throws RuntimeException("재고 복구 실패")

            assertThrows<OrderCancelFailedException> {
                orderCancelFacade.cancel(command)
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 취소 처리")
    inner class CancelCoupon {
        @Test
        @DisplayName("쿠폰이 있는 경우 쿠폰 취소 처리를 한다")
        fun cancelCouponIfPresent() {
            val couponId = CouponMock.id()
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = couponId)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verify {
                orderCouponProcessor.cancelCoupon(CancelCouponUseProcessorCommand(couponId))
            }
        }

        @Test
        @DisplayName("쿠폰이 없는 경우 쿠폰 취소 처리를 하지 않는다")
        fun doNotCancelCouponIfNotPresent() {
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = null)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verify(exactly = 0) {
                orderCouponProcessor.cancelCoupon(any())
            }
        }

        @Test
        @DisplayName("쿠폰 취소 처리 중 예외가 발생하면 다른 처리는 계속하지만 전체 실패로 처리된다")
        fun couponCancelFail() {
            val couponId = CouponMock.id()
            val order = OrderMock.view(status = OrderStatus.FAILED, couponId = couponId)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order
            every { orderCouponProcessor.cancelCoupon(any()) } throws RuntimeException("쿠폰 취소 실패")

            assertThrows<OrderCancelFailedException> {
                orderCancelFacade.cancel(command)
            }

            verify(exactly = 1) {
                orderCouponProcessor.cancelCoupon(any())
                orderProductProcessor.restoreOrderProductStock(any())
                orderPaymentProcessor.cancelPayment(any())
            }
        }
    }

    @Nested
    @DisplayName("상품 재고 복구 처리")
    inner class RestoreProductStock {
        @Test
        @DisplayName("상품 재고를 productId 순서대로 복구한다")
        fun restoreStockInOrder() {
            val orderProducts = listOf(
                OrderMock.productView(productId = 3L),
                OrderMock.productView(productId = 1L),
                OrderMock.productView(productId = 2L),
            )
            val order = OrderMock.view(status = OrderStatus.FAILED, products = orderProducts)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verifyOrder {
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    assertThat(it.productId.value).isEqualTo(1L)
                })
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    assertThat(it.productId.value).isEqualTo(2L)
                })
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    assertThat(it.productId.value).isEqualTo(3L)
                })
            }
        }

        @Test
        @DisplayName("일부 상품 재고 복구가 실패해도 다른 상품의 재고는 복구한다")
        fun restoreOtherProductsWhenOneFail() {
            val failProductId = ProductId(1L)
            val successProductId = ProductId(2L)
            val orderProducts = listOf(
                OrderMock.productView(productId = failProductId.value),
                OrderMock.productView(productId = successProductId.value)
            )
            val order = OrderMock.view(status = OrderStatus.FAILED, products = orderProducts)
            val command = CancelOrderFacadeCommand(order)

            every { orderService.get(order.id.value) } returns order
            every {
                orderProductProcessor.restoreOrderProductStock(match {
                    it.productId == failProductId
                })
            } throws RuntimeException("재고 복구 실패")

            assertThrows<OrderCancelFailedException> {
                orderCancelFacade.cancel(command)
            }

            verify {
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    assertThat(it.productId).isEqualTo(failProductId)
                })
                orderProductProcessor.restoreOrderProductStock(withArg<RestoreStockOrderProductProcessorCommand> {
                    assertThat(it.productId).isEqualTo(successProductId)
                })
                orderPaymentProcessor.cancelPayment(any())
            }
        }
    }

    @Nested
    @DisplayName("결제 취소 처리")
    inner class CancelPayment {
        @Test
        @DisplayName("결제 취소 처리")
        fun cancelPayment() {
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

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
        @DisplayName("결제 취소 처리 중 예외가 발생하면 다른 처리는 계속하지만 전체 실패로 처리된다")
        fun cancelPaymentFail() {
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order
            every { orderPaymentProcessor.cancelPayment(any()) } throws RuntimeException("결제 취소 실패")

            assertThrows<OrderCancelFailedException> {
                orderCancelFacade.cancel(command)
            }

            verify {
                orderProductProcessor.restoreOrderProductStock(any())
                orderPaymentProcessor.cancelPayment(any())
            }
        }
    }

    @Nested
    @DisplayName("주문 상태 변경 처리")
    inner class MarkOrderFailHandled {
        @Test
        @DisplayName("주문 취소 모든 작업이 성공하면, 주문을 FAIL_HANDLED 상태로 변경한다")
        fun markOrderFailHandled() {
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order

            orderCancelFacade.cancel(command)

            verify {
                orderLifecycleProcessor.markFailHandled(MarkOrderFailHandledProcessorCommand(order.id))
            }
        }

        @Test
        @DisplayName("주문 취소의 작업 중 일부가 실패하면 주문을 FAIL_HANDLED 상태로 변경하지 않는다")
        fun doNotMarkOrderFailHandled() {
            val order = OrderMock.view(status = OrderStatus.FAILED)
            val command = CancelOrderFacadeCommand(order)
            every { orderService.get(order.id.value) } returns order
            every { orderProductProcessor.restoreOrderProductStock(any()) } throws RuntimeException("재고 복구 실패")

            assertThrows<OrderCancelFailedException> {
                orderCancelFacade.cancel(command)
            }

            verify(exactly = 0) {
                orderLifecycleProcessor.markFailHandled(any())
            }
        }
    }
}
