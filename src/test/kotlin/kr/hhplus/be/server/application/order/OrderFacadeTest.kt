package kr.hhplus.be.server.application.order

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.application.order.command.*
import kr.hhplus.be.server.application.order.processor.OrderCouponProcessor
import kr.hhplus.be.server.application.order.processor.OrderLifecycleProcessor
import kr.hhplus.be.server.application.order.processor.OrderPaymentProcessor
import kr.hhplus.be.server.application.order.processor.OrderProductProcessor
import kr.hhplus.be.server.application.order.result.OrderCreationProcessorResult
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.OrderCommandMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderFacadeTest {

    @InjectMockKs
    private lateinit var orderFacade: OrderFacade

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var orderLifecycleProcessor: OrderLifecycleProcessor

    @MockK(relaxed = true)
    private lateinit var orderProductProcessor: OrderProductProcessor

    @MockK(relaxed = true)
    private lateinit var orderCouponProcessor: OrderCouponProcessor

    @MockK(relaxed = true)
    private lateinit var orderPaymentProcessor: OrderPaymentProcessor

    @Nested
    @DisplayName("주문")
    inner class Order {
        @Test
        @DisplayName("쿠폰이 있는 주문 처리")
        fun withCoupon() {
            val couponId = CouponMock.id()
            val command = OrderCommandMock.facade(couponId = couponId).also { spyk(it) }
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, couponId = couponId)
            every { orderLifecycleProcessor.createOrder(any()) } returns OrderCreationProcessorResult(orderId)
            every { orderService.get(orderId.value) } returns order

            val result = orderFacade.order(command)

            assertThat(result.order).isEqualTo(order)
            verifyOrder {
                command.validate()
                orderLifecycleProcessor.createOrder(
                    CreateOrderProcessorCommand(command.userId)
                )
                command.productsToOrder.forEach {
                    orderProductProcessor.placeOrderProduct(
                        PlaceOrderProductProcessorCommand(
                            orderId = orderId,
                            productId = it.productId,
                            unitPrice = it.unitPrice,
                            quantity = it.quantity,
                        )
                    )
                }
                orderService.get(orderId.value)
                orderCouponProcessor.applyCouponToOrder(
                    ApplyCouponProcessorCommand(
                        orderId = orderId,
                        userId = order.userId,
                        couponId = couponId.value
                    )
                )
                orderService.get(orderId.value)
                orderPaymentProcessor.processPayment(
                    PayOrderProcessorCommand(
                        orderId = orderId,
                        userId = order.userId,
                        totalAmount = order.totalAmount
                    )
                )
                orderService.get(orderId.value)
            }
        }

        @Test
        @DisplayName("쿠폰이 없는 주문 처리")
        fun withoutCoupon() {
            val command = OrderCommandMock.facade(couponId = null).also { spyk(it) }
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId, couponId = null)
            every { orderLifecycleProcessor.createOrder(any()) } returns OrderCreationProcessorResult(orderId)
            every { orderService.get(orderId.value) } returns order

            val result = orderFacade.order(command)

            assertThat(result.order).isEqualTo(order)
            verifyOrder {
                command.validate()
                orderLifecycleProcessor.createOrder(
                    CreateOrderProcessorCommand(command.userId)
                )
                command.productsToOrder.forEach {
                    orderProductProcessor.placeOrderProduct(
                        PlaceOrderProductProcessorCommand(
                            orderId = orderId,
                            productId = it.productId,
                            unitPrice = it.unitPrice,
                            quantity = it.quantity,
                        )
                    )
                }
                orderService.get(orderId.value)
                orderPaymentProcessor.processPayment(
                    PayOrderProcessorCommand(
                        orderId = orderId,
                        userId = order.userId,
                        totalAmount = order.totalAmount
                    )
                )
                orderService.get(orderId.value)
            }
            verify(exactly = 0) {
                orderCouponProcessor.applyCouponToOrder(any())
            }
        }

        @Test
        @DisplayName("주문 상품 확보는 productId 기준으로 정렬되어 처리")
        fun processOrderProductsInOrder() {
            val command = mockk<OrderFacadeCommand>(relaxed = true)
            every { command.productsToOrder } returns listOf(
                OrderCommandMock.productToOrder(productId = 3L, unitPrice = 10_000.toBigDecimal(), quantity = 2),
                OrderCommandMock.productToOrder(productId = 1L, unitPrice = 15_000.toBigDecimal(), quantity = 1),
                OrderCommandMock.productToOrder(productId = 2L, unitPrice = 10_000.toBigDecimal(), quantity = 3)
            )
            val orderId = OrderMock.id()
            val order = OrderMock.view(id = orderId)

            every { orderService.createOrder(any<CreateOrderCommand>()) } returns orderId
            every { orderService.get(orderId.value) } returns order

            orderFacade.order(command)

            verifyOrder {
                orderProductProcessor.placeOrderProduct(withArg<PlaceOrderProductProcessorCommand> {
                    assertThat(it.productId).isEqualTo(1L)
                })
                orderProductProcessor.placeOrderProduct(withArg<PlaceOrderProductProcessorCommand> {
                    assertThat(it.productId).isEqualTo(2L)
                })
                orderProductProcessor.placeOrderProduct(withArg<PlaceOrderProductProcessorCommand> {
                    assertThat(it.productId).isEqualTo(3L)
                })
            }
        }

        @Test
        @DisplayName("주문 중 예외 발생 시 실패 처리")
        fun handleFailure() {
            val command = OrderCommandMock.facade().also { spyk(it) }
            val orderId = OrderMock.id()
            val failedException = RuntimeException("테스트 예외")
            every { orderLifecycleProcessor.createOrder(any()) } returns OrderCreationProcessorResult(orderId)
            every { orderProductProcessor.placeOrderProduct(any()) } throws failedException

            shouldThrow<RuntimeException> {
                orderFacade.order(command)
            }

            verify {
                orderLifecycleProcessor.failOrder(
                    FailOrderProcessorCommand(
                        orderId = orderId,
                        reason = "테스트 예외"
                    )
                )
            }
        }
    }
}
