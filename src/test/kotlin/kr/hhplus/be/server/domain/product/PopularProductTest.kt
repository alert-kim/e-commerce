package kr.hhplus.be.server.domain.product

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.util.TimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PopularProductTest {

    @Test
    fun `인기 상품은 정해진 수보다 같거나 작아야 한다`() {
        val size = Arb.int(0.. PopularProducts.MAX_SIZE).next()
        val porducts = List(size) {
            ProductMock.product()
        }

        shouldNotThrowAny {
            PopularProducts(products = porducts)
        }
    }

    @Test
    fun `인기 상품 조회의 개수가 맞지 않다면 예외가 발생한다`() {
        val porducts = List(PopularProducts.MAX_SIZE + 1) {
            ProductMock.product()
        }

        shouldThrow<IllegalArgumentException> {
            PopularProducts(products = porducts)
        }
    }

    @Test
    fun `인기 상품 조회의 시작 날짜는 KST기준 2일 전이다`() {
        val result = PopularProducts.getStartDay()

        assertThat(result).isEqualTo(LocalDate.now(TimeZone.KSTId).minusDays(2))
    }

    @Test
    fun `인기 상품 조회의 종료 날짜는 KST기준 오늘이다`() {
        val result = PopularProducts.getEndDay()

        assertThat(result).isEqualTo(LocalDate.now(TimeZone.KSTId))
    }
}
