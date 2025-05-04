package kr.hhplus.be.server.domain.product.stat

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.util.TimeZone
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PopularProductIdsTest {

    @Test
    fun `인기 상품은 정해진 수보다 같거나 작아야 한다`() {
        val size = Arb.Companion.int(0..PopularProductsIds.Companion.MAX_SIZE).next()
        val productIds = List(size) {
            ProductMock.id()
        }

        shouldNotThrowAny {
            PopularProductsIds(value = productIds)
        }
    }

    @Test
    fun `인기 상품 조회의 개수가 맞지 않다면 예외가 발생한다`() {
        val productIds = List(PopularProductsIds.Companion.MAX_SIZE + 1) {
            ProductMock.id()
        }

        shouldThrow<IllegalArgumentException> {
            PopularProductsIds(value = productIds)
        }
    }

    @Test
    fun `인기 상품 조회의 시작 날짜는 KST기준 2일 전이다`() {
        val result = PopularProductsIds.getStartDay()

        Assertions.assertThat(result).isEqualTo(LocalDate.now(TimeZone.KSTId).minusDays(2))
    }

    @Test
    fun `인기 상품 조회의 종료 날짜는 KST기준 오늘이다`() {
        val result = PopularProductsIds.getEndDay()

        Assertions.assertThat(result).isEqualTo(LocalDate.now(TimeZone.KSTId))
    }
}
