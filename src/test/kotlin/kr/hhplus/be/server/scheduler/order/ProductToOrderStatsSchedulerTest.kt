package kr.hhplus.be.server.scheduler.order

import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.common.util.TimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("오늘 날짜로 상품 판매 집계를 수행한다")
    fun aggregate() {
        val today = LocalDate.now(TimeZone.KSTId)

        orderProductStatsScheduler.aggregateDaily()

        verify {
            productFacade.aggregate(
                withArg<AggregateProductDailySalesFacadeCommand> {
                    assertThat(it.date).isEqualTo(today)
                }
            )
            productFacade.getPopularProducts()
        }
    }
}
