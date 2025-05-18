package kr.hhplus.be.server.infra.product.ranking

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RedisProductSaleScoreCalculatorTest {

    @Nested
    @DisplayName("점수 계산")
    inner class Calculate {
        @Test
        @DisplayName("상품 판매 수량과 주문 건수를 합산하여 점수를 계산한다")
        fun basic() {
            val entry = ProductSaleRankingEntry(
                date = LocalDate.now(),
                productId = ProductId(1L),
                quantity = 5,
                orderCount = 3
            )

            val score = RedisProductSaleScoreCalculator.calculate(entry)

            assertThat(score).isEqualTo(8.0)
        }
    }
}
