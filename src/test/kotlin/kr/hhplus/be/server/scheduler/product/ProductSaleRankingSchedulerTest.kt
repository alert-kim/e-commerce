package kr.hhplus.be.server.scheduler.product

import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.RenewPopularProductFacadeCommand
import kr.hhplus.be.server.common.util.TimeZone
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductSaleRankingSchedulerTest {
    private val productFacade = mockk<ProductFacade>(relaxed = true)
    private val scheduler = ProductSaleRankingScheduler(
        productFacade = productFacade,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(productFacade)
    }

    @Nested
    @DisplayName("인기 상품 랭킹 갱신")
    inner class Aggregate {
        @Test
        @DisplayName("오늘 일자에 해당하는 인기 상품 랭킹을 갱신한다")
        fun success() {
            val today = LocalDate.now(TimeZone.KSTId)

            scheduler.aggregate()

            verify {
                productFacade.renew(RenewPopularProductFacadeCommand(today))
            }
        }
    }
}
