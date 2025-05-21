package kr.hhplus.be.server.infra.product.persistence.ranking

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.infra.product.persistence.ranking.ProductRankingRedisKeyGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductRankingRedisKeyGeneratorTest {

    @Nested
    @DisplayName("일별 키 생성")
    inner class DailyKeyTests {

        @Test
        @DisplayName("특정 날짜에 대한 일별 키 생성")
        fun dailyKey() {
            val date = LocalDate.of(2025, 5, 15)

            val key = ProductRankingRedisKeyGenerator.dailyKey(date)

            assertThat(key).isEqualTo("product:sale:rank:2025-05-15")
        }
    }

    @Nested
    @DisplayName("합산 키 생성")
    inner class UnionKeyTests {

        @Test
        @DisplayName("시작 날짜와 종료 날짜에 대한 합산 키 생성")
        fun createUnionKey() {
            val startDate = LocalDate.of(2025, 5, 12)
            val endDate = LocalDate.of(2025, 5, 15)

            val key = ProductRankingRedisKeyGenerator.unionKey(startDate, endDate)

            assertThat(key).isEqualTo("product:sale:rank:union:2025-05-12:2025-05-15")
        }

        @Test
        @DisplayName("시작 날짜가 종료 날짜보다 큰 경우 예외 발생")
        fun error() {
            val startDate = LocalDate.of(2025, 5, 15)
            val endDate = LocalDate.of(2025, 5, 11)

            shouldThrow<IllegalArgumentException> {
                ProductRankingRedisKeyGenerator.unionKey(startDate, endDate)
            }
        }
    }
}
