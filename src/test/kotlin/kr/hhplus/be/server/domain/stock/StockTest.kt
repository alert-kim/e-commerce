package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.stock.exception.InvalidStockQuantityToAllocateException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StockTest {

    @Test
    fun `allocate - 재고를 할당한다`() {
        val initialQuantity = 10
        val decreaseQuantity = 3
        val stock = StockMock.stock(
            quantity = initialQuantity
        )
        val beforeUpdate = stock.updatedAt

        val result = stock.allocate(decreaseQuantity)

        assertThat(result.productId).isEqualTo(stock.productId)
        assertThat(result.quantity).isEqualTo(decreaseQuantity)
        assertThat(stock.quantity).isEqualTo(initialQuantity - decreaseQuantity)
        assertThat(stock.updatedAt).isAfter(beforeUpdate)
    }

    @Test
    fun `allocate - 감소시키려는 수량이 현재 재고보다 많으면 예외 발생`() {
        val initialQuantity = 10
        val decreaseQuantity = 15
        val stock = StockMock.stock(
            quantity = initialQuantity
        )

        assertThrows<OutOfStockException> {
            stock.allocate(decreaseQuantity)
        }
        assertThat(stock.quantity).isEqualTo(initialQuantity)
    }

    @Test
    fun `allocate - 감소시키려는 수량이 0이하라면 예외 발생`() {
        val decreaseQuantity = 0
        val stock = StockMock.stock()

        assertThrows<InvalidStockQuantityToAllocateException> {
            stock.allocate(decreaseQuantity)
        }
    }
}
