package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.ConsumeOrderEventsFacadeCommand
import kr.hhplus.be.server.application.order.command.OrderFacadeCommand
import kr.hhplus.be.server.application.order.command.SendOrderFacadeCommand
import kr.hhplus.be.server.application.order.result.OrderResult
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.OrderSnapshot
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.order.result.CreateOrderResult
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductsView
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.domain.stock.result.AllocatedStockResult
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.mock.*
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
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns CreateOrderResult(orderId)
        every { productService.getAllByIds(any()) } returns ProductsView(products)
        every { stockService.allocate(any<AllocateStocksCommand>()) } returns AllocatedStockResult(stocks)
        every { couponService.use(UseCouponCommand(couponId.value, userId)) } returns usedCoupon
        every { balanceService.use(any<UseBalanceCommand>()) } returns usedAmount
        every { paymentService.pay(any<PayCommand>()) } returns payment
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result).isInstanceOf(OrderResult.Single::class.java)
        assertThat(result.value).isEqualTo(order)
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
        every { orderService.createOrder(any<CreateOrderCommand>()) } returns CreateOrderResult(orderId)
        every { productService.getAllByIds(any()) } returns ProductsView(products)
        every { stockService.allocate(any<AllocateStocksCommand>()) } returns AllocatedStockResult(stocks)
        every { balanceService.use(any<UseBalanceCommand>()) } returns usedAmount
        every { paymentService.pay(any<PayCommand>()) } returns payment
        every { orderService.get(orderId.value) } returns order

        val result = orderFacade.order(command)

        assertThat(result).isInstanceOf(OrderResult.Single::class.java)
        assertThat(result.value).isEqualTo(order)
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

    @Test
    fun `sendOrderCompletionData - 주문 완료 데이터를 전송한다`() {
        val orderSnapshot = OrderSnapshot.from(OrderMock.order())
        val command = SendOrderFacadeCommand(orderSnapshot)

        orderFacade.sendOrderCompletionData(command)

        verify {
            orderService.sendOrderCompleted(SendOrderCompletedCommand(orderSnapshot))
        }
    }

    @Test
    fun `consumeEvent - 주문 이벤트를 소비한다`() {
        val consumerId = "test-consumer"
        val events = listOf(OrderMock.event())
        val command = ConsumeOrderEventsFacadeCommand(consumerId, events)

        orderFacade.consumeEvent(command)

        verify {
            orderService.consumeEvent(ConsumeOrderEventCommand.of(consumerId, events))
        }
    }

    @Test
    fun `getAllEventsNotConsumedInOrder - 소비되지 않은 주문 이벤트를 조회한다`() {
        val consumerId = "test-consumer"
        val eventType = OrderEventType.COMPLETED
        val events = listOf(OrderMock.event())
        every { orderService.getAllEventsNotConsumedInOrder(consumerId, eventType) } returns events

        // when
        val result = orderFacade.getAllEventsNotConsumedInOrder(consumerId, eventType)

        // then
        assertThat(result).isInstanceOf(OrderResult.Events::class.java)
        assertThat(result.value).isEqualTo(events)
        verify {
            orderService.getAllEventsNotConsumedInOrder(consumerId, eventType)
        }
    }
}