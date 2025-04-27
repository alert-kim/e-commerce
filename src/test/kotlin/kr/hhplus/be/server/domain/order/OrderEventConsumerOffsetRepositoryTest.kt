package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetId
import kr.hhplus.be.server.domain.order.event.OrderEventConsumerOffsetRepository
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.util.OrderEventConsumerOffsetAsserts.Companion.assertOrderEventConsumerOffset
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OrderEventConsumerOffsetRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var repository: OrderEventConsumerOffsetRepository

    @Test
    fun `save - 저장 및 조회 테스트`() {
        val consumerId = "test-consumer"
        val eventType = OrderEventType.COMPLETED
        val eventId = OrderMock.eventId()
        val offset = OrderMock.eventConsumerOffset(consumerId = consumerId, eventId = eventId, eventType = eventType)

        val saved = repository.save(offset)

        assertOrderEventConsumerOffset(offset).isEqualTo(saved)
    }

    @Test
    fun `find - ID로 조회시 존재하는 오프셋 반환`() {
        val offset = OrderMock.eventConsumerOffset()
        repository.save(offset)

        val found = repository.find(offset.id)

        assertOrderEventConsumerOffset(found).isEqualTo(offset)
    }

    @Test
    fun `find - 존재하지 않는 id로 조회시 null 반환`() {
        val nonExistingConsumerId = "non-existing-consumer"
        val eventType = OrderEventType.COMPLETED

        val foundOffset =
            repository.find(OrderEventConsumerOffsetId(consumerId = nonExistingConsumerId, eventType = eventType))

        assertThat(foundOffset).isNull()
    }
}
