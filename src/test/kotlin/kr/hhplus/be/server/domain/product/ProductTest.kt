package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.excpetion.RequiredProductIdException
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val product = ProductMock.product(id = ProductMock.id())

        val result = product.requireId()

        assertThat(result).isEqualTo(product.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredProductIdException 발생`() {
        val product = ProductMock.product(id = null)

        assertThrows<RequiredProductIdException> {
            product.requireId()
        }
    }
}
