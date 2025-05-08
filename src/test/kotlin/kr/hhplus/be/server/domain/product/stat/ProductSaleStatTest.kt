package kr.hhplus.be.server.domain.product.stat

import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.product.excpetion.RequiredProductStatIdException
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("ProductSaleStat 엔티티 테스트")
class ProductSaleStatTest {

    @Nested
    @DisplayName("new")
    inner class New {
        @Test
        @DisplayName("ProductSaleStat 생성")
        fun success() {
            val productId = ProductMock.id()
            val quantity = 5

            val stat = ProductSaleStat.new(
                productId = productId,
                quantity = quantity,
            )

            shouldThrow<RequiredProductStatIdException> {
                stat.id()
            }
            assertThat(stat.productId).isEqualTo(productId)
            assertThat(stat.quantity).isEqualTo(quantity)
        }
    }

    @Nested
    @DisplayName("id")
    inner class Id {

        @Test
        @DisplayName("ID가 할당된 경우 ProductSaleStatId 반환")
        fun idExists() {
            val id = ProductMock.saleStatId()
            val stat = ProductMock.saleStat(id = id)

            val result = stat.id()

            assertThat(result).isEqualTo(id)
        }

        @Test
        @DisplayName("ID가 할당되지 않은 경우 RequiredProductIdException 발생")
        fun idIsNull() {
            val stat = ProductMock.saleStat(id = null)

            shouldThrow<RequiredProductStatIdException> {
                stat.id()
            }
        }
    }
}
