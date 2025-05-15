package kr.hhplus.be.server.domain.product.stat

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PopularProductIdsTest {

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
    @DisplayName("인기 상품 조회의 개수가 맞지 않다면 예외가 발생한다")
    fun errorIfCountNotMatch() {
        val productIds = List(PopularProductsIds.Companion.MAX_SIZE + 1) {
            ProductMock.id()
        }

        shouldThrow<IllegalArgumentException> {
            PopularProductsIds(value = productIds)
        }
    }

    @Test
    @DisplayName("인기 상품 시작 날짜는 기준일 2일 전이다")
    fun startDate() {
        val date = LocalDate.now()

        val result = PopularProductsIds.getStartDateFromBaseDate(date)

        Assertions.assertThat(result).isEqualTo(date.minusDays(2))
    }
}
