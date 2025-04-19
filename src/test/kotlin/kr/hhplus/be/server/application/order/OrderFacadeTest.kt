package kr.hhplus.be.server.domain.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.result.CouponUsedResult
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.result.PayResult
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStockAllocated
import kr.hhplus.be.server.domain.product.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.product.result.AllocatedStockResult
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.PaymentMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockKExtension::class)
class OrderFacadeTest {

    @InjectMockKs
    private lateinit var orderFacade: OrderFacade

    @MockK(relaxed = true)
    private lateinit var balanceService: BalanceService

    @MockK(relaxed = true)
    private lateinit var couponService: CouponService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var paymentService: PaymentService

    @MockK(relaxed = true)
    private lateinit var productService: ProductService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @Test
    fun `order - 쿠폰이 있는 경우`() {
        val couponId = CouponMock.id()
        val coupon = CouponMock.coupon(id = couponId)
        val command = orderFacadeCommand(couponId = couponId.value)
        val userId = UserId(command.userId)
        val user = UserMock.user(id = userId)
        val orderId = OrderMock.id()
        val stockAllocated = command.orderProducts.map {
            ProductStockAllocated(
                productId = ProductId(it.productId),
                quantity = it.quantity,
                unitPrice = it.unitPrice
            )
        }
        val payment = PaymentMock.payment(
            orderId = orderId,
            userId = userId,
            amount = command.totalAmount,
            createdAt = Instant.now()
        )
        val order = OrderMock.order(id = orderId, userId = userId, couponId = couponId)
        val orderSheet = createOrderSheet(orderId, command)
        every { userService.get(command.userId) } returns user
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns CreateOrderResult(orderId)
        every { productService.allocateStocks(any<AllocateStocksCommand>()) } returns AllocatedStockResult(
            stocks = stockAllocated,
        )
        every { couponService.use(UseCouponCommand(couponId.value, userId)) } returns CouponUsedResult(coupon)
        every { paymentService.pay(any<PayCommand>()) } returns PayResult(payment)
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result).isEqualTo(
            OrderQueryModel.from(order)
        )
        verifyOrder {
            userService.get(command.userId)
            orderService.createOrder(
                CreateOrderCommand(
                    userId = userId,
                )
            )
            productService.allocateStocks(
                AllocateStocksCommand(
                    needStocks = command.orderProducts.map {
                        AllocateStocksCommand.NeedStock(
                            productId = it.productId,
                            quantity = it.quantity
                        )
                    }
                )
            )
            orderService.placeStock(
                PlaceStockCommand(
                    orderId = orderId,
                    stocks = stockAllocated,
                )
            )
            couponService.use(
                UseCouponCommand(
                    couponId = couponId.value,
                    userId = userId
                )
            )
            orderService.applyCoupon(
                ApplyCouponCommand(
                    orderId = orderId,
                    coupon = coupon,
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
                    amount = order.totalAmount
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
        val command = orderFacadeCommand(couponId = null)
        val userId = UserId(command.userId)
        val user = UserMock.user(id = userId)
        val orderId = OrderMock.id()
        val stockAllocated = command.orderProducts.map {
            ProductStockAllocated(
                productId = ProductId(it.productId),
                quantity = it.quantity,
                unitPrice = it.unitPrice
            )
        }
        val payment = PaymentMock.payment(
            orderId = orderId,
            userId = userId,
            amount = command.totalAmount,
        )
        val order = OrderMock.order(id = orderId, userId = userId, couponId = null)
        every { userService.get(command.userId) } returns user
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns CreateOrderResult(orderId)
        every { productService.allocateStocks(any<AllocateStocksCommand>()) } returns AllocatedStockResult(
            stocks = stockAllocated,
        )
        every { paymentService.pay(any<PayCommand>()) } returns PayResult(payment)
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result).isEqualTo(
            OrderQueryModel.from(order)
        )
        verifyOrder {
            userService.get(command.userId)
            orderService.createOrder(
                CreateOrderCommand(
                    userId = userId,
                )
            )
            productService.allocateStocks(
                AllocateStocksCommand(
                    needStocks = command.orderProducts.map {
                        AllocateStocksCommand.NeedStock(
                            productId = it.productId,
                            quantity = it.quantity
                        )
                    }
                )
            )
            orderService.placeStock(
                PlaceStockCommand(
                    orderId = orderId,
                    stocks = stockAllocated,
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
                    amount = order.totalAmount
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
            couponService.use(any<UseCouponCommand>())
            orderService.applyCoupon(any<ApplyCouponCommand>())
        }
    }

    private fun orderFacadeCommand(
        couponId: Long? = null
    ): OrderFacadeCommand =
        OrderFacadeCommand(
            userId = 1L,
            orderProducts = listOf(
                OrderFacadeCommand.OrderProduct(
                    productId = 1L,
                    quantity = 2,
                    unitPrice = BigDecimal.valueOf(10_000),
                    totalPrice = BigDecimal.valueOf(20_000)
                ),
                OrderFacadeCommand.OrderProduct(
                    productId = 2L,
                    quantity = 1,
                    unitPrice = BigDecimal.valueOf(30_000),
                    totalPrice = BigDecimal.valueOf(30_000)
                ),
            ),
            couponId = couponId,
            originalAmount = BigDecimal.valueOf(50_000),
            discountAmount = BigDecimal.valueOf(10_000),
            totalAmount = BigDecimal.valueOf(60_000)
        )

    private fun createOrderSheet(
        orderId: OrderId,
        command: OrderFacadeCommand
    ): OrderSheet =
        OrderSheet(
            orderId = orderId,
            userId = UserId(command.userId),
            orderProducts = command.orderProducts.map {
                OrderSheet.OrderProduct(
                    productId = it.productId,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    totalPrice = it.totalPrice
                )
            },
            couponId = command.couponId,
            originalAmount = command.originalAmount,
            discountAmount = command.discountAmount,
            totalAmount = command.totalAmount
        )
}
