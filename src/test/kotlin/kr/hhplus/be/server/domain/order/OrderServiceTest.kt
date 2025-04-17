package kr.hhplus.be.server.domain.order

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import kr.hhplus.be.server.domain.order.event.OrderEventRepository
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.mock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @InjectMockKs
    private lateinit var service: OrderService

    @MockK(relaxed = true)
    private lateinit var repository: OrderRepository

    @MockK(relaxed = true)
    private lateinit var eventRepository: OrderEventRepository

    @MockK(relaxed = true)
    private lateinit var client: OrderSnapshotClient

    @MockK(relaxed = true)
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        clearMocks(repository, eventRepository)
    }

    @Test
    fun `create - 주문 생성`() {
        val orderId = OrderMock.id()
        val userId = UserMock.id()
        every { repository.save(any()) } returns orderId

        val result = service.createOrder(CreateOrderCommand(userId))

        assertThat(result.orderId).isEqualTo(orderId)
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
        val order = OrderMock.order(
            id = orderId,
            status = OrderStatus.READY,
            products = emptyList(),
            originalAmount = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            couponId = null,
        )
        val orderSheet = mockk<OrderSheet>(relaxed = true)
        val stocks = List(3) {
            ProductMock.stockAllocated()
        }
        val totalAmount = stocks.fold(BigDecimal.ZERO) { acc, stock ->
            acc.add(stock.unitPrice.multiply(BigDecimal(stock.quantity)))
        }
        val command = PlaceStockCommand(orderSheet, stocks)
        every { repository.findById(orderId.value) } returns order
        every { orderSheet.orderId } returns orderId
        every { repository.save(any()) } returns orderId

        service.placeStock(command)

        verify {
            repository.save(withArg {
                assertAll(
                    { assertThat(it.id).isEqualTo(orderId) },
                    { assertThat(it.status).isEqualTo(OrderStatus.STOCK_ALLOCATED) },
                    { assertThat(it.originalAmount).isEqualByComparingTo(totalAmount) },
                    { assertThat(it.totalAmount).isEqualByComparingTo(totalAmount) },
                    { assertThat(it.discountAmount).isEqualByComparingTo(BigDecimal.ZERO) },
                    { assertThat(it.couponId).isNull() },
                    { assertThat(it.products).hasSize(stocks.size) }
                )
                it.products.forEach { product ->
                    val stock = stocks.find { it.productId == product.productId }
                    assertAll(
                        { assertThat(stock).isNotNull() },
                        { assertThat(product.orderId).isEqualTo(orderId) },
                        { assertThat(product.productId).isEqualTo(stock?.productId) },
                        { assertThat(product.quantity).isEqualTo(stock?.quantity) },
                        { assertThat(product.unitPrice).isEqualByComparingTo(stock?.unitPrice) },
                        {
                            assertThat(product.totalPrice).isEqualByComparingTo(
                                stock?.unitPrice?.multiply(
                                    BigDecimal(
                                        stock.quantity
                                    )
                                )
                            )
                        }
                    )
                }
                assertThat(it.updatedAt).isAfter(it.createdAt)
            })
        }
    }

    @Test
    fun `placeStock - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        val orderSheet = mockk<OrderSheet>(relaxed = true)
        val stocks = List(3) {
            ProductMock.stockAllocated()
        }
        val command = PlaceStockCommand(orderSheet, stocks)
        every { repository.findById(orderId.value) } returns null
        every { orderSheet.orderId } returns orderId

        shouldThrow<NotFoundOrderException> {
            service.placeStock(command)
        }

        verify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `applyCoupon  - 쿠폰 적용`() {
        val orderId = OrderMock.id()
        val order = mockk<Order>()
        val orderSheet = mockk<OrderSheet>(relaxed = true)
        val command = ApplyCouponCommand(
            orderSheet = orderSheet,
            coupon = CouponMock.coupon()
        )
        every { repository.findById(orderId.value) } returns order
        every { repository.save(any()) } returns orderId

        service.applyCoupon(command)

        verify {
            repository.save(any())
        }
    }

    @Test
    fun `applyCoupon - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        val orderSheet = mockk<OrderSheet>(relaxed = true)
        every { repository.findById(orderId.value) } returns null
        every { orderSheet.orderId } returns orderId

        shouldThrow<NotFoundOrderException> {
            service.applyCoupon(ApplyCouponCommand(
                orderSheet = orderSheet,
                coupon = CouponMock.coupon(),
            ))
        }

        verify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `pay  - 결제`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId)
        val payment = PaymentMock.payment(
            orderId = orderId,
            amount = BigDecimal.ZERO,
        )
        val command = PayOrderCommand(
            payment = payment,
        )
        every { repository.findById(orderId.value) } returns order
        every { repository.save(any()) } returns orderId

        service.pay(command)

        verify {
            repository.save(any())
            eventRepository.save(withArg {
                assertThat(it.orderId).isEqualTo(orderId)
                assertThat(it.type).isEqualTo(OrderEventType.COMPLETED)
            })
        }
    }


    @Test
    fun `pay - 주문을 찾을 수 없음 - NotFoundOrderException발생`() {
        val orderId = OrderMock.id()
        val payment = PaymentMock.payment(
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

        verify(exactly = 0) {
            repository.save(any())
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
