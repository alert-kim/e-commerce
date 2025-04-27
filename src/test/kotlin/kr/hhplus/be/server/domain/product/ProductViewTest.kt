package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class ProductViewTest {
    @Test
    fun `상품 정보를 올바르게 변환한다`() {
        val product = ProductMock.product()

        val result = ProductView.from(product)

        assertAll(
            { assertThat(result.id).isEqualTo(product.id) },
            { assertThat(result.name).isEqualTo(product.name) },
            { assertThat(result.description).isEqualTo(product.description) },
            { assertThat(result.price.value).isEqualByComparingTo(product.price) },
            { assertThat(result.createdAt).isEqualTo(product.createdAt) },
        )
    }

    @Test
    fun `해당 상품의 아이디가 null이면 RequiredProductIdException가 발생한다`() {
        val product = ProductMock.product(id = null)

        assertThrows<RequiredProductIdException> {
            ProductView.from(product)
        }
    }
}
