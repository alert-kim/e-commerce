package kr.hhplus.be.server.scheduler.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.ConsumeOrderEventFacadeCommand
import kr.hhplus.be.server.application.order.command.SendOrderFacadeCommand
import kr.hhplus.be.server.domain.order.event.OrderEventType
import kr.hhplus.be.server.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderCompletionSendingSchedulerTest {

    @InjectMockKs
    private lateinit var orderCompletionSendingScheduler: OrderCompletionSendingScheduler

    @MockK(relaxed = true)
    private lateinit var orderFacade: OrderFacade

    @Test
    fun `주문 완료 데이터 전송 - 아직 처리하지 않은 주문 완료 이벤트 스냅샷 정보를 가져와 전송한다`() {
        val orderEvents = List(10) {
            OrderMock.event()
        }
        every {
            orderFacade.getAllEventsNotHandledInOrder(
                OrderCompletionSendingScheduler.SCHEDULER_ID,
                OrderEventType.COMPLETED
            )
        } returns orderEvents

        orderCompletionSendingScheduler.send()

        val orderSnapshots = orderEvents.map { it.snapshot }
        verify(exactly = orderEvents.size) {
            orderFacade.sendOrderCompletionData(
                withArg<SendOrderFacadeCommand> {
                    assertThat(it.orderSnapshot).isIn(orderSnapshots)
                }
            )
            orderFacade.consumeEvent(
                withArg<ConsumeOrderEventFacadeCommand> {
                    assertThat(it.consumerId).isEqualTo(OrderCompletionSendingScheduler.SCHEDULER_ID)
                    assertThat(it.event).isIn(orderEvents)
                }
            )
        }
    }
}
