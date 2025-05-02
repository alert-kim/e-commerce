package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.exception.RequiredOrderEventIdException
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderEventTest {
    @Test
    fun `id - id가 null이 아닌 경우 id 반환`() {
        val id = OrderMock.eventId()
        val event = OrderMock.event(id = id)

        val result = event.id()

        assertThat(result).isEqualTo(id)
    }

    @Test
    fun `id - id가 null이면 RequiredOrderEventIdException 발생`() {
        val event = OrderMock.event(id = null)

        assertThrows<RequiredOrderEventIdException> {
            event.id()
        }
    }
}
