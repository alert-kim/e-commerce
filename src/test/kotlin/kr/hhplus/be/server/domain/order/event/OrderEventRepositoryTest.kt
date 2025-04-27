package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.order.repository.OrderEventRepository
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.util.OrderEventAssert.Companion.assertOrderEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class OrderEventRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: OrderEventRepository

    @Test
    fun `save - 주문 이벤트를 저장`() {
        val event = OrderMock.event(id = null)

        val saved = repository.save(event)

        assertOrderEvent(saved).isEqualTo(event)
    }

    @Test
    fun `findAllByIdAsc - 모든 주문 이벤트를 ID 오름차순으로 조회`() {
        val events = List(3) {
            repository.save(OrderMock.event(id = null))
        }

        val result = repository.findAllByIdAsc()

        assertThat(result).hasSize(events.size)
        result.forEachIndexed { index, order ->
            assertOrderEvent(order).isEqualTo(events[index])
        }
    }

    @Test
    fun `findAllByIdGreaterThanOrderByIdAsc - 특정 ID보다 큰 주문 이벤트를 ID 오름차순으로 조회`() {
        val targetOrderId = repository.save(OrderMock.event(id = null)).id()
        val events = List(3) {
            repository.save(OrderMock.event(id = null))
        }

        val result = repository.findAllByIdGreaterThanOrderByIdAsc(targetOrderId)

        assertThat(result).hasSize(events.size)
        result.forEachIndexed { index, order ->
            assertOrderEvent(order).isEqualTo(events[index])
        }
    }
}
