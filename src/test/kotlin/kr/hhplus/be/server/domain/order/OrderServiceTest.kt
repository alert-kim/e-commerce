package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.domain.product.result.PurchasableProduct
import kr.hhplus.be.server.domain.stock.result.AllocatedStock
import kr.hhplus.be.server.testutil.mock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @InjectMockKs
    private lateinit var service: OrderService

    @MockK(relaxed = true)
    private lateinit var repository: OrderRepository

    @MockK(relaxed = true)
    private lateinit var publisher: ApplicationEventPublisher

    @MockK(relaxed = true)
    private lateinit var client: OrderSnapshotClient

    @BeforeEach
    fun setUp() {
        clearMocks(repository, publisher, client)
    }

    @Test
    fun `create - 주문 생성`() {
        val orderId = OrderMock.id()
        every { repository.save(any()) } returns OrderMock.order(id = orderId)
        val userId = UserMock.id()

        val result = service.createOrder(CreateOrderCommand(userId))

        assertThat(result).isEqualTo(orderId)
        verify {
            repository.save(withArg {
                assertThat(it.userId).isEqualTo(userId)
                assertThat(it.status).isEqualTo(OrderStatus.READY)
                assertThat(it.originalAmount).isEqualByComparingTo(BigDecimal.ZERO)
                assertThat(it.discountAmount).isEqualByComparingTo(BigDecimal.ZERO)
                assertThat(it.totalAmount).isEqualByComparingTo(BigDecimal.ZERO)
                assertThat(it.couponId).isNull()
                assertThat(it.products).isEmpty()
            })
        }
    }

    @Test
    fun `placeStock - 주문 상품 생성`() {
        val orderId = OrderMock.id()
        val order = mockk<Order>(relaxed = true)
        val command = PlaceStockCommand(
            orderId = orderId,
            preparedProductForOrder = List(3) {
                val productId = ProductMock.id()
                PlaceStockCommand.PreparedProductForOrder(
                    product = PurchasableProduct(
                        id = productId,
                        price = ProductPrice(1000.toBigDecimal()),
                    ),
                    stock = AllocatedStock(
                        productId = productId,
                        quantity = Arb.int(1..5).next(),
                    ),
                )
            }
        )
        every { repository.findById(orderId.value) } returns order

        service.placeStock(command)

        verify {
            command.preparedProductForOrder.forEachIndexed { index, preparedProductForOrder ->
                order.placeStock(
                    productId = preparedProductForOrder.productId,
                    quantity = preparedProductForOrder.stock.quantity,
                    unitPrice = preparedProductForOrder.product.price,
                )
            }
        }
    }

    @Test
    fun `placeStock - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        val command = PlaceStockCommand(
            orderId = orderId,
            preparedProductForOrder = listOf(
                ProductMock.id().let {
                    PlaceStockCommand.PreparedProductForOrder(
                        product = ProductMock.purchasableProduct(id = it),
                        stock = StockMock.allocated(productId = it),
                    )
                }

            )
        )
        every { repository.findById(orderId.value) } returns null

        shouldThrow<NotFoundOrderException> {
            service.placeStock(command)
        }
    }

    @Test
    fun `applyCoupon  - 쿠폰 적용`() {
        val orderId = OrderMock.id()
        val order = mockk<Order>(relaxed = true)
        val command = ApplyCouponCommand(
            orderId = orderId,
            usedCoupon = CouponMock.usedCoupon()
        )
        every { repository.findById(orderId.value) } returns order

        service.applyCoupon(command)

        verify {
            order.applyCoupon(command.usedCoupon)
        }
    }

    @Test
    fun `applyCoupon - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        every { repository.findById(orderId.value) } returns null

        shouldThrow<NotFoundOrderException> {
            service.applyCoupon(
                ApplyCouponCommand(
                    orderId = orderId,
                    usedCoupon = CouponMock.usedCoupon(),
                )
            )
        }
    }

    @Test
    fun `pay  - 결제`() {
        val orderId = OrderMock.id()
        val order = mockk<Order>(relaxed = true)
        val payment = PaymentMock.view(
            orderId = orderId,
            amount = BigDecimal.ZERO,
        )
        val command = PayOrderCommand(
            payment = payment,
        )
        every { order.id() } returns orderId
        every { repository.findById(orderId.value) } returns order

        service.pay(command)

        verify {
            order.pay()
            publisher.publishEvent(withArg<OrderCompletedEvent> {
                assertThat(it.orderId).isEqualTo(orderId)
            })
        }
    }


    @Test
    fun `pay - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        val payment = PaymentMock.view(
            orderId = orderId,
            amount = BigDecimal.ZERO,
        )
        val command = PayOrderCommand(
            payment = payment,
        )
        every { repository.findById(orderId.value) } returns null

        shouldThrow<NotFoundOrderException> {
            service.pay(command)
        }
    }

    @Test
    fun `sendOrderCompleted - 주문 완료 전송`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId, status = OrderStatus.COMPLETED)
        val command = SendOrderCompletedCommand(
            orderSnapshot = OrderSnapshot.from(order),
        )

        service.sendOrderCompleted(command)

        verify {
            client.send(command.orderSnapshot)
        }
    }

    @Test
    fun `sendOrderCompleted - 완료 되지 않은 주문 에러 - InvalidOrderStatusException 발생`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId, status = OrderStatus.READY)
        val command = SendOrderCompletedCommand(
            orderSnapshot = OrderSnapshot.from(order),
        )

        assertThrows<InvalidOrderStatusException> {
            service.sendOrderCompleted(command)
        }

        verify(exactly = 0) {
            client.send(any())
        }
    }

    @Test
    fun `get - 주문 조회`() {
        val orderId = OrderMock.id()
        every { repository.findById(orderId.value) } returns OrderMock.order(id = orderId)

        val result = service.get(orderId.value)

        assertThat(result.id).isEqualTo(orderId)
        verify {
            repository.findById(orderId.value)
        }
    }

    @Test
    fun `get - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        every { repository.findById(orderId.value) } returns null

        shouldThrow<NotFoundOrderException> {
            service.get(orderId.value)
        }
    }
}
