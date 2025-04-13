package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class OrderTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val order = OrderMock.order(id = OrderMock.id())

        val result = order.requireId()

        assertThat(result).isEqualTo(order.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredOrderIdException 발생`() {
        val order = OrderMock.order(id = null)

        assertThrows<RequiredOrderIdException> {
            order.requireId()
        }
    }

    @Test
    fun `new - 해당 유저아이디를 가진, 0원으로 초기화된 주문을 생성한다`() {
        val userId = UserMock.id()

        val order = Order.new(userId)

        assertAll(
            { assertThat(order.userId).isEqualTo(userId) },
            { assertThat(order.status).isEqualTo(OrderStatus.READY) },
            { assertThat(order.originalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.discountAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.totalAmount).isEqualByComparingTo(BigDecimal.ZERO) },
            { assertThat(order.products).isEmpty() },
            { assertThat(order.couponId).isNull() },
        )
    }
}
