package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.domain.stock.exception.InvalidStockQuantityToAllocateException
import kr.hhplus.be.server.domain.stock.exception.OutOfStockException
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class StockTest {

    @Nested
    @DisplayName("재고 할당")
    inner class Allocate {

        @Test
        @DisplayName("재고를 할당한다")
        fun allocateStock() {
            val initialQuantity = 10
            val decreaseQuantity = 3
            val stock = StockMock.stock(quantity = initialQuantity)
            val beforeUpdate = stock.updatedAt

            val result = stock.allocate(decreaseQuantity)

            assertThat(result.productId).isEqualTo(stock.productId)
            assertThat(result.quantity).isEqualTo(decreaseQuantity)
            assertThat(stock.quantity).isEqualTo(initialQuantity - decreaseQuantity)
            assertThat(stock.updatedAt).isAfter(beforeUpdate)
        }

        @Test
        @DisplayName("감소 수량이 재고보다 많으면 예외 발생")
        fun allocateExceedsStock() {
            val initialQuantity = 10
            val decreaseQuantity = 15
            val stock = StockMock.stock(quantity = initialQuantity)

            assertThrows<OutOfStockException> {
                stock.allocate(decreaseQuantity)
            }
            assertThat(stock.quantity).isEqualTo(initialQuantity)
        }

        @Test
        @DisplayName("감소 수량이 0 이하이면 예외 발생")
        fun allocateInvalidQuantity() {
            val decreaseQuantity = 0
            val stock = StockMock.stock()

            assertThrows<InvalidStockQuantityToAllocateException> {
                stock.allocate(decreaseQuantity)
            }
        }
    }

    @Nested
    @DisplayName("재고 복구")
    inner class Restore {

        @Test
        @DisplayName("재고를 복구한다")
        fun restoreStock() {
            val initialQuantity = 10
            val restoreQuantity = 5
            val stock = StockMock.stock(quantity = initialQuantity)
            val beforeUpdate = stock.updatedAt

            stock.restore(restoreQuantity)

            assertThat(stock.quantity).isEqualTo(initialQuantity + restoreQuantity)
            assertThat(stock.updatedAt).isAfter(beforeUpdate)
        }

        @Test
        @DisplayName("복구 수량이 0 이하이면 예외 발생")
        fun restoreInvalidQuantity() {
            val stock = StockMock.stock()

            assertThrows<InvalidStockQuantityToAllocateException> {
                stock.restore(0)
            }
            assertThrows<InvalidStockQuantityToAllocateException> {
                stock.restore(-1)
            }
        }
    }
}
