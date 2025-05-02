package kr.hhplus.be.server.domain.order.command

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.order.event.OrderEventId
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConsumeOrderEventCommandSpec {
    @Test
    fun `이벤트가 한 개인 경우 해당 이벤트 소비 명령으로 변경 된다`() {
        val event = OrderMock.event()

        val command = ConsumeOrderEventCommand.of(
            consumerId = "consumerId",
            events = listOf(event),
        )

        assertThat(command.event).isEqualTo(event)
    }

    @Test
    fun `이벤트가 여러 개인 경우 가장 나중의 이벤트 소비 명령으로 변경 된다`() {
        val lastEventId = OrderEventId(3L)
        val event = OrderMock.event(id = lastEventId)
        val eventType = event.type
        val otherEvents = listOf(
            OrderMock.event(id = OrderEventId(1L), type = eventType),
            OrderMock.event(id = OrderEventId(2L), type = eventType),
        )

        val command = ConsumeOrderEventCommand.of(
            consumerId = "consumerId",
            events = otherEvents + event,
        )

        assertThat(command.event.id()).isEqualTo(lastEventId)
    }

    @Test
    fun `이벤트가 비어 있는 경우 해당 에러가 발생한다`() {
        shouldThrow<IllegalArgumentException> {
            ConsumeOrderEventCommand.of(
                consumerId = "consumerId",
                events = emptyList(),
            )
        }
    }
}
