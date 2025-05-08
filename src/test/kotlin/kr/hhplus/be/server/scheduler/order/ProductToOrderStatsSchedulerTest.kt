package kr.hhplus.be.server.scheduler.order

import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductToOrderStatsSchedulerTest {

    private val productFacade = mockk<ProductFacade>(relaxed = true)
    private val orderProductStatsScheduler = OrderProductStatsScheduler(
        productFacade = productFacade,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(productFacade)
    }

    @Test
    fun `일일 집계 - 오늘 날짜로 상품 판매 집계를 수행한다`() {
        val today = LocalDate.now()

        orderProductStatsScheduler.aggregateDaily()

        verify {
            productFacade.aggregate(
                withArg<AggregateProductDailySalesFacadeCommand> {
                    assertThat(it.date).isEqualTo(today)
                }
            )
        }
    }
}
