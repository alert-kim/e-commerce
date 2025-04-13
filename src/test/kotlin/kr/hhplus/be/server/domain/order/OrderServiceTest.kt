package kr.hhplus.be.server.domain.order

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.order.command.CreateOrderCommand
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
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
}
