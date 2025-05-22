package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {

    @Nested
    @DisplayName("id 메서드")
    inner class Id {

        @Test
        @DisplayName("id가 null이 아닌 경우 id 반환")
        fun returnId() {
            val id = ProductMock.id()
            val product = ProductMock.product(id = id)

            val result = product.id()

            assertThat(result).isEqualTo(id)
        }

        @Test
        @DisplayName("id가 null이면 RequiredProductIdException 발생")
        fun throwExceptionWhenNull() {
            val product = ProductMock.product(id = null)

            assertThrows<RequiredProductIdException> {
                product.id()
            }
        }
    }
}
