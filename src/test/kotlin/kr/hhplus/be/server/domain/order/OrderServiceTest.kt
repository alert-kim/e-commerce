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
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import kr.hhplus.be.server.domain.order.event.OrderFailedEvent
import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.testutil.mock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
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

    @Nested
    @DisplayName("주문 생성")
    inner class CreateOrder {
        @Test
        @DisplayName("사용자 ID로 주문을 생성")
        fun createOrder() {
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
    }

    @Nested
    @DisplayName("상품 주문 배치")
    inner class PlaceStock {
        @Test
        @DisplayName("상품 주문 배치")
        fun place() {
            val orderId = OrderMock.id()
            val order = mockk<Order>(relaxed = true)
            val product = ProductMock.purchasableProduct()
            val stock = StockMock.allocated(productId = product.id)
            val command = PlaceStockCommand(
                orderId = orderId,
                product = product,
                stock = stock
            )
            every { repository.findById(orderId.value) } returns order

            service.placeStock(command)

            verify {
                order.placeStock(
                    productId = command.product.id,
                    quantity = command.stock.quantity,
                    unitPrice = command.product.price
                )
            }
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 NotFoundOrderException 예외 발생")
        fun notFoundOrder() {
            val orderId = OrderMock.id()
            val product = ProductMock.purchasableProduct()
            val stock = StockMock.allocated(productId = product.id)
            val command = PlaceStockCommand(
                orderId = orderId,
                product = product,
                stock = stock
            )
            every { repository.findById(orderId.value) } returns null

            shouldThrow<NotFoundOrderException> {
                service.placeStock(command)
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 적용")
    inner class ApplyCoupon {
        @Test
        @DisplayName("쿠폰을 적용")
        fun apply() {
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
        @DisplayName("주문을 찾을 수 없으면 NotFoundOrderException 예외가 발생한다")
        fun notFoundOrder() {
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
    }

    @Nested
    @DisplayName("주문 결제")
    inner class PayOrder {
        @Test
        @DisplayName("주문을 결제 처리")
        fun pay() {
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
        @DisplayName("주문을 찾을 수 없으면 NotFoundOrderException 예외가 발생")
        fun notFoundOrder() {
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
    }

    @Nested
    @DisplayName("주문 실패 처리")
    inner class FailOrder {
        @Test
        @DisplayName("주문을 실패 상태로 변경하고 이벤트를 저장")
        fun failOrder() {
            val orderId = OrderMock.id()
            val order = mockk<Order>(relaxed = true)
            val command = FailOrderCommand(orderId, "테스트 실패 사유")
            every { repository.findById(orderId.value) } returns order
            every { order.id() } returns orderId
            every { order.isFailed() } returns false

            service.failOrder(command)

            verify {
                order.fail()
                publisher.publishEvent(withArg<OrderFailedEvent> {
                    assertThat(it.orderId).isEqualTo(orderId)
                })
            }
        }

        @Test
        @DisplayName("이미 실패 상태인 경우 이벤트를 저장하지 않음")
        fun alreadyFailed() {
            val orderId = OrderMock.id()
            val order = mockk<Order>(relaxed = true)
            val command = FailOrderCommand(orderId, "테스트 실패 사유")
            every { repository.findById(orderId.value) } returns order
            every { order.isFailed() } returns true

            service.failOrder(command)

            verify(exactly = 0) {
                order.fail()
                publisher.publishEvent(any())
            }
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 NotFoundOrderException 예외가 발생")
        fun notFoundOrder() {
            val orderId = OrderMock.id()
            val command = FailOrderCommand(orderId, "테스트 실패 사유")
            every { repository.findById(orderId.value) } returns null

            shouldThrow<NotFoundOrderException> {
                service.failOrder(command)
            }
        }
    }

    @Nested
    @DisplayName("주문 완료 전송")
    inner class SendOrderCompleted {
        @Test
        @DisplayName("완료된 주문을 전송")
        fun sendCompletedOrder() {
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
        @DisplayName("완료되지 않은 주문을 전송하면 InvalidOrderStatusException 예외가 발생한다")
        fun notCompletedOrder() {
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
    }

    @Nested
    @DisplayName("주문 조회")
    inner class GetOrder {
        @Test
        @DisplayName("주문 ID로 주문을 조회한다")
        fun getOrderById() {
            val orderId = OrderMock.id()
            every { repository.findById(orderId.value) } returns OrderMock.order(id = orderId)

            val result = service.get(orderId.value)

            assertThat(result.id).isEqualTo(orderId)
            verify {
                repository.findById(orderId.value)
            }
        }

        @Test
        @DisplayName("주문을 찾을 수 없으면 NotFoundOrderException 예외가 발생한다")
        fun notFoundOrder() {
            val orderId = OrderMock.id()
            every { repository.findById(orderId.value) } returns null

            shouldThrow<NotFoundOrderException> {
                service.get(orderId.value)
            }
        }
    }
}
