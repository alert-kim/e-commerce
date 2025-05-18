package kr.hhplus.be.server.domain.product.stat

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PopularProductIdsTest {

    @Nested
    @DisplayName("생성")
    inner class Create {
        @Test
        @DisplayName("인기 상품은 정해진 수보다 같거나 작아야 한다")
        fun count() {
            val size = Arb.Companion.int(0..PopularProductsIds.Companion.MAX_SIZE).next()
            val productIds = List(size) {
                ProductMock.id()
            }

            shouldNotThrowAny {
                PopularProductsIds(value = productIds)
            }
        }

        @Test
        @DisplayName("인기 상품은 정해진 수보다 크면 IllegalArgumentExceptiond가 발생한다")
        fun invalid() {
            val productIds = List(PopularProductsIds.Companion.MAX_SIZE + 1) {
                ProductMock.id()
            }

            shouldThrow<IllegalArgumentException> {
                PopularProductsIds(value = productIds)
            }
        }
    }

    @Nested
    @DisplayName("시작일 계산")
    inner class StartDate {
        @Test
        @DisplayName("기준일로부터 2일 전의 날짜를 반환한다")
        fun calculate() {
            val date = LocalDate.now()

            val result = PopularProductsIds.getStartDateFromBaseDate(date)

            Assertions.assertThat(result).isEqualTo(date.minusDays(2))
        }
    }
}
