package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.ApplyCouponCommand
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductsView
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
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
    private lateinit var couponService: CouponService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var paymentService: PaymentService

    @MockK(relaxed = true)
    private lateinit var productService: ProductService

    @MockK(relaxed = true)
    private lateinit var stockService: StockService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

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
        val purchasableProduct = products.map {
            ProductMock.purchasableProduct(
                id = it.id,
                price = it.price.value,
            )
        }
        val stocks = command.productsToOrder.map {
            AllocatedStock(
                productId = ProductId(it.productId),
                quantity = it.quantity,
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
        every { productService.getAllByIds(any()) } returns ProductsView(products)
        every { stockService.allocate(any<AllocateStocksCommand>()) } returns stocks
        every { couponService.use(UseCouponCommand(couponId.value, userId)) } returns usedCoupon
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
            stockService.allocate(
                AllocateStocksCommand(
                    command.productsToOrder.associate {
                        ProductId(it.productId) to it.quantity
                    }
                )
            )
            orderService.placeStock(
                PlaceStockCommand.of(
                    orderId = orderId,
                    products = purchasableProduct,
                    stocks = stocks,
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
                    usedCoupon = usedCoupon,
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
        val products = command.productsToOrder.map {
            ProductMock.view(
                id = ProductId(it.productId),
                price = it.unitPrice,
            )
        }
        val purchasableProduct = products.map {
            ProductMock.purchasableProduct(
                id = it.id,
                price = it.price.value,
            )
        }
        val stocks = command.productsToOrder.map {
            AllocatedStock(
                productId = ProductId(it.productId),
                quantity = it.quantity,
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
        )
        val order = OrderMock.view(id = orderId, userId = userId, couponId = null)
        every { userService.get(command.userId) } returns user
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns orderId
        every { productService.getAllByIds(any()) } returns ProductsView(products)
        every { stockService.allocate(any<AllocateStocksCommand>()) } returns stocks
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
            stockService.allocate(
                AllocateStocksCommand(
                    command.productsToOrder.associate {
                        ProductId(it.productId) to it.quantity
                    }
                )
            )
            orderService.placeStock(
                PlaceStockCommand.of(
                    orderId = orderId,
                    products = purchasableProduct,
                    stocks = stocks,
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
            couponService.use(any<UseCouponCommand>())
            orderService.applyCoupon(any<ApplyCouponCommand>())
        }
    }
}
