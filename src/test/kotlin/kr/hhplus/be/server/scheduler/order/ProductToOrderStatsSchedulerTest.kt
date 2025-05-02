package kr.hhplus.be.server.scheduler.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.command.ConsumeOrderEventsFacadeCommand
import kr.hhplus.be.server.application.order.result.GetOrderFacadeEventResult
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProductToOrderStatsSchedulerTest {

    @InjectMockKs
    private lateinit var orderProductStatsScheduler: OrderProductStatsScheduler

    @MockK(relaxed = true)
    private lateinit var productFacade: ProductFacade

    @MockK(relaxed = true)
    private lateinit var orderFacade: OrderFacade

    @Test
    fun `주문 완료된 판매 상품 집계 - 아직 처리하지 않은 주문 완료 이벤트 스냅샷 정보를 가져와 집계한다`() {
        val orderEvents = List(10) {
            OrderMock.event()
        }
        every {
            orderFacade.getAllEventsNotConsumedInOrder(
                OrderProductStatsScheduler.SCHEDULER_ID,
                OrderProductStatsScheduler.eventType,
            )
        } returns GetOrderFacadeEventResult.List(orderEvents)

        orderProductStatsScheduler.aggregate()

        val orderProducts = orderEvents.flatMap { it.snapshot.orderProducts }
        verify {
            productFacade.aggregate(
                withArg<AggregateProductDailySalesFacadeCommand> {
                    it.sales.forEach { sale ->
                        assertThat(sale).isIn(orderProducts)
                    }
                }
            )
            orderFacade.consumeEvent(
                withArg<ConsumeOrderEventsFacadeCommand> {
                    assertThat(it.consumerId).isEqualTo(OrderProductStatsScheduler.SCHEDULER_ID)
                    assertThat(it.events).isEqualTo(orderEvents)
                }
            )
        }
    }
}
