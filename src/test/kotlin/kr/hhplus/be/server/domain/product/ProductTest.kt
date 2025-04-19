package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.balance.exception.InsufficientBalanceException
import kr.hhplus.be.server.domain.product.excpetion.OutOfStockProductException
import org.junit.jupiter.api.assertAll

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

    @Test
    fun `allocateStock - 재고 할당`() {
        val originalQuantity = 20L
        val product = ProductMock.product(
            stock = ProductMock.stock(quantity = originalQuantity),
        )
        val quantityToAllocate = 5

        val result = product.allocateStock(quantityToAllocate)

        assertAll(
            { assertThat(product.stock.quantity).isEqualTo(originalQuantity - quantityToAllocate) },
            { assertThat(result.productId).isEqualTo(product.id) },
            { assertThat(result.quantity).isEqualTo(quantityToAllocate) },
            { assertThat(result.unitPrice).isEqualTo(product.price) }
        )
    }

    @Test
    fun `allocateStock - 재고 부족시 OutOfStockProductException`() {
        val originalQuantity = 20L
        val product = ProductMock.product(
            stock = ProductMock.stock(quantity = originalQuantity),
        )
        val quantityToAllocate = originalQuantity + 1

        assertThrows<OutOfStockProductException> {
            product.allocateStock(quantityToAllocate.toInt())
        }
    }
}
