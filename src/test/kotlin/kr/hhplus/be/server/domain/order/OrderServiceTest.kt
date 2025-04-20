package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.command.*
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetRepository
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderRepository
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
    private lateinit var eveentConsumerOffsetRepository: OrderEventConsumerOffsetRepository

    @MockK(relaxed = true)
    private lateinit var client: OrderSnapshotClient

    @BeforeEach
    fun setUp() {
        clearMocks(repository, eventRepository)
    }

    @Test
    fun `create - 주문 생성`() {
        val orderId = OrderMock.id()
        every { repository.save(any()) } returns orderId
        val userId = UserMock.id()

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
        val stocks = List(3) {
            ProductMock.stockAllocated()
        }
        val totalAmount = stocks.fold(BigDecimal.ZERO) { acc, stock ->
            acc.add(stock.unitPrice.multiply(BigDecimal(stock.quantity)))
        }
        val command = PlaceStockCommand(orderId, stocks)
        every { repository.findById(orderId.value) } returns order
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
        val stocks = List(3) {
            ProductMock.stockAllocated()
        }
        val command = PlaceStockCommand(orderId, stocks)
        every { repository.findById(orderId.value) } returns null

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
        val order = mockk<Order>(relaxed = true)
        val command = ApplyCouponCommand(
            orderId = orderId,
            usedCoupon = CouponMock.usedCoupon()
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
        every { repository.findById(orderId.value) } returns null

        shouldThrow<NotFoundOrderException> {
            service.applyCoupon(
                ApplyCouponCommand(
                    orderId = orderId,
                    usedCoupon = CouponMock.usedCoupon(),
                )
            )
        }

        verify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `pay  - 결제`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId)
        val payment = PaymentMock.view(
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
    fun `consumeEvent - 이벤트 처리 (이벤트 개수 1) - 첫 처리인 경우 새로운 offset저장`() {
        val eventId = OrderMock.eventId()
        val event = OrderMock.event(id = eventId)
        val command = ConsumeOrderEventCommand(
            consumerId = "test",
            event = event,
        )
        every { eveentConsumerOffsetRepository.find(command.consumerId, event.type) } returns null

        service.consumeEvent(command)

        verify {
            eveentConsumerOffsetRepository.save(withArg {
                assertThat(it.consumerId).isEqualTo(command.consumerId)
                assertThat(it.value).isEqualTo(eventId)
                assertThat(it.eventType).isEqualTo(event.type)
            })
        }
    }

    @Test
    fun `consumeEvent - 이벤트 처리 (이벤트 개수1) - 이미 offset이 있는 경우 update`() {
        val oldEventId = OrderMock.eventId()
        val eventId = OrderMock.eventId()
        val event = OrderMock.event(id = eventId)
        val command = ConsumeOrderEventCommand(
            consumerId = "test",
            event = event,
        )
        every { eveentConsumerOffsetRepository.find(command.consumerId, event.type) } returns
                OrderMock.eventConsumerOffset(eventId = oldEventId)

        service.consumeEvent(command)

        verify {
            eveentConsumerOffsetRepository.update(withArg {
                assertThat(it.consumerId).isEqualTo(command.consumerId)
                assertThat(it.value).isEqualTo(eventId)
            })
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

    @Test
    fun `getAllEventsNotConsumedInOrder - 처리하지 않은 이벤트 조회 - 처리했던 이벤트 offset이 있을 경우`() {
        val consumerId = "test"
        val eventType = OrderEventType.COMPLETED
        val events = List(3) { OrderMock.event() }
        val offset = OrderMock.eventConsumerOffset()
        every { eveentConsumerOffsetRepository.find(consumerId, eventType) } returns offset
        every { eventRepository.findAllByIdGreaterThanOrderByIdAsc(offset.value) } returns events

        val result = service.getAllEventsNotConsumedInOrder(consumerId, eventType)

        assertThat(result).hasSize(events.size)
        result.forEachIndexed { index, orderEvent ->
            val expect = events[index]
            assertThat(orderEvent.id).isEqualTo(expect.id)
        }
    }

    @Test
    fun `getAllEventsNotConsumedInOrder - 처리하지 않은 이벤트 조회 - 처리했던 이벤트 offset이 없을 경우`() {
        val consumerId = "test"
        val eventType = OrderEventType.COMPLETED
        val events = List(2) { OrderMock.event() }
        every { eveentConsumerOffsetRepository.find(consumerId, eventType) } returns null
        every { eventRepository.findAllOrderByIdAsc() } returns events

        val result = service.getAllEventsNotConsumedInOrder(consumerId, eventType)

        assertThat(result).hasSize(events.size)
        result.forEachIndexed { index, orderEvent ->
            val expect = events[index]
            assertThat(orderEvent.id).isEqualTo(expect.id)
        }
    }
}
