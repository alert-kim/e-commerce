package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.order.exception.NotFoundOrderException
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @InjectMockKs
    private lateinit var service: OrderService

    @MockK(relaxed = true)
    private lateinit var repository: OrderRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
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
}
