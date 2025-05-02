package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class OrderEventConsumerOffsetTest {

    @Test
    fun `new - OrderEventConsumerOffset 생성`() {
        val consumerId = "test-consumer"
        val eventId = OrderMock.eventId()
        val eventType = OrderEventType.COMPLETED

        val offset = OrderEventConsumerOffset.new(consumerId, eventId, eventType)

        assertThat(offset.id.consumerId).isEqualTo(consumerId)
        assertThat(offset.id.eventType).isEqualTo(eventType)
        assertThat(offset.eventId).isEqualTo(eventId)
    }

    @Test
    fun `update - eventId와 updatedAt을 갱신`() {
        val newEventId = OrderEventId(2L)
        val oldUpdatedAt = Instant.now()
        val offset = OrderMock.eventConsumerOffset(
            eventId = OrderEventId(1L),
            updatedAt = oldUpdatedAt,
        )

        offset.update(newEventId)

        assertThat(offset.eventId).isEqualTo(newEventId)
        assertThat(offset.updatedAt).isAfter(oldUpdatedAt)
    }
}
