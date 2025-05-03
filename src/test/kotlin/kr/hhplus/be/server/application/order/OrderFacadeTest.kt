package kr.hhplus.be.server.application.order

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.application.order.command.ApplyCouponProcessorCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.testutil.mock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class OrderFacadeTest {

    @InjectMockKs
    private lateinit var orderFacade: OrderFacade

    @MockK(relaxed = true)
    private lateinit var balanceService: BalanceService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var paymentService: PaymentService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @MockK(relaxed = true)
    private lateinit var orderProductProcessor: OrderProductProcessor
    
    @MockK(relaxed = true)
    private lateinit var orderCouponProcessor: OrderCouponProcessor

    @Test
    fun `order - 쿠폰이 있는 경우`() {
        val couponId = CouponMock.id()
        val usedCoupon = CouponMock.usedCoupon(id = couponId)
        val command = OrderCommandMock.facade(couponId = couponId).also { spyk(it) }
        val userId = UserId(command.userId)
        val user = UserMock.view(id = userId)
        val orderId = OrderMock.id()
        val products = command.productsToOrder.map {
            ProductMock.view(
                id = ProductId(it.productId),
                price = it.unitPrice,
            )
        }
        val usedAmount = UsedBalanceAmount(
            balanceId = BalanceMock.id(),
            amount = BalanceAmount.of(command.totalAmount),
        )
        val payment = PaymentMock.view(
            orderId = orderId,
            userId = userId,
            amount = command.totalAmount,
            createdAt = Instant.now()
        )
        val order = OrderMock.view(id = orderId, userId = userId, couponId = couponId)
        every { userService.get(command.userId) } returns user
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns orderId
        every { balanceService.use(any<UseBalanceCommand>()) } returns usedAmount
        every { paymentService.pay(any<PayCommand>()) } returns payment
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result.order).isEqualTo(order)
        verifyOrder {
            command.validate()
            userService.get(command.userId)
            orderService.createOrder(
                CreateOrderCommand(
                    userId = userId,
                )
            )
            command.productsToOrder.forEach {
                orderProductProcessor.placeOrderProduct(PlaceOrderProductProcessorCommand(
                    orderId = orderId,
                    productId = it.productId,
                    unitPrice = it.unitPrice,
                    quantity = it.quantity,
                ))
            }
            orderCouponProcessor.applyCouponToOrder(
                ApplyCouponProcessorCommand(
                    orderId = orderId,
                    userId = userId,
                    couponId = couponId.value
                )
            )
            orderService.get(orderId.value)
            balanceService.use(
                UseBalanceCommand(
                    userId = userId,
                    amount = order.totalAmount
                )
            )
            paymentService.pay(
                PayCommand(
                    userId = userId,
                    orderId = orderId,
                    amount = usedAmount,
                )
            )
            orderService.pay(
                PayOrderCommand(
                    payment = payment
                )
            )
            orderService.get(orderId.value)
        }
    }

    @Test
    fun `order - 쿠폰이 없는 경우`() {
        val command = OrderCommandMock.facade(couponId = null).also { spyk(it) }
        val userId = UserId(command.userId)
        val user = UserMock.view(id = userId)
        val orderId = OrderMock.id()
        val usedAmount = UsedBalanceAmount(
            balanceId = BalanceMock.id(),
            amount = BalanceAmount.of(command.totalAmount),
        )
        val payment = PaymentMock.view(
            orderId = orderId,
            userId = userId,
            amount = command.totalAmount,
        )
        val order = OrderMock.view(id = orderId, userId = userId, couponId = null)
        every { userService.get(command.userId) } returns user
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns orderId
        every { balanceService.use(any<UseBalanceCommand>()) } returns usedAmount
        every { paymentService.pay(any<PayCommand>()) } returns payment
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result.order).isEqualTo(order)
        verifyOrder {
            command.validate()
            userService.get(command.userId)
            orderService.createOrder(
                CreateOrderCommand(
                    userId = userId,
                )
            )

            balanceService.use(
                UseBalanceCommand(
                    userId = userId,
                    amount = order.totalAmount
                )
            )
            paymentService.pay(
                PayCommand(
                    userId = userId,
                    orderId = orderId,
                    amount = usedAmount,
                )
            )
            orderService.pay(
                PayOrderCommand(
                    payment = payment
                )
            )
            orderService.get(orderId.value)
        }
        verify(exactly = 0) {
            orderService.applyCoupon(any<ApplyCouponCommand>())
        }
    }

    @Test
    fun `order - placeStocks - productId 기준으로 정렬되어 처리된다`() {
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
}
