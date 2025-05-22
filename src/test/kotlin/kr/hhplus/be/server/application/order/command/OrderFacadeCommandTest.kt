package kr.hhplus.be.server.application.order.command

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderProductQuantityException
import kr.hhplus.be.server.testutil.mock.OrderCommandMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderFacadeCommandTest {

    @Nested
    @DisplayName("유효성 검증")
    inner class Validate {
        @Test
        @DisplayName("모든 가격 계산이 맞으면 에러가 발생하지 않는다")
        fun validPrices() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 3,
                    unitPrice = 3_000.toBigDecimal(),
                    totalPrice = 9_000.toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 19_000.toBigDecimal(),
                discountAmount = BigDecimal.ZERO,
                totalAmount = 19_000.toBigDecimal(),
            )

            shouldNotThrowAny {
                command.validate()
            }
        }

        @Test
        @DisplayName("주문 상품이 비어 있으면 에러가 발생한다")
        fun emptyProducts() {
            val command = OrderCommandMock.facade(
                productsToOrder = emptyList(),
            )

            shouldThrow<InvalidOrderProductQuantityException> {
                command.validate()
            }
        }

        @Test
        @DisplayName("주문 상품의 가격 합이 총 원가격과 다르면 에러가 발생한다")
        fun productPriceSumMismatch() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 3,
                    unitPrice = 3_000.toBigDecimal(),
                    totalPrice = 9_000.toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 18_000.toBigDecimal(),
                discountAmount = BigDecimal.ZERO,
                totalAmount = 18_000.toBigDecimal(),
            )

            shouldThrow<InvalidOrderPriceException> {
                command.validate()
            }
        }

        @Test
        @DisplayName("원가격이 할인금액과 총비용의 합과 다르면 에러가 발생한다")
        fun totalPriceMismatch() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 3,
                    unitPrice = 3_000.toBigDecimal(),
                    totalPrice = 9_000.toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 19_000.toBigDecimal(),
                discountAmount = 1_000.toBigDecimal(),
                totalAmount = 19_000.toBigDecimal(),
            )

            shouldThrow<InvalidOrderPriceException> {
                command.validate()
            }
        }

        @Test
        @DisplayName("주문 상품 수량이 0이면 에러가 발생한다")
        fun zeroQuantity() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 0,
                    unitPrice = 3_000.toBigDecimal(),
                    totalPrice = 0.toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 10_000.toBigDecimal(),
                discountAmount = BigDecimal.ZERO,
                totalAmount = 10_000.toBigDecimal(),
            )

            shouldThrow<InvalidOrderProductQuantityException> {
                command.validate()
            }
        }

        @Test
        @DisplayName("단위가격이 0보다 작으면 에러가 발생한다")
        fun negativeUnitPrice() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = (-1000).toBigDecimal(),
                    totalPrice = (-1000).toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 9_000.toBigDecimal(),
                discountAmount = BigDecimal.ZERO,
                totalAmount = 9_000.toBigDecimal(),
            )

            shouldThrow<InvalidOrderPriceException> {
                command.validate()
            }
        }

        @Test
        @DisplayName("수량 × 단위가격이 총가격과 다르면 에러가 발생한다")
        fun productTotalPriceMismatch() {
            val productsToOrder = listOf(
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 2_000.toBigDecimal(),
                    totalPrice = 3_000.toBigDecimal(),
                ),
                OrderCommandMock.productToOrder(
                    quantity = 1,
                    unitPrice = 10_000.toBigDecimal(),
                    totalPrice = 10_000.toBigDecimal(),
                ),
            )
            val command = OrderCommandMock.facade(
                productsToOrder = productsToOrder,
                originalAmount = 13_000.toBigDecimal(),
                discountAmount = BigDecimal.ZERO,
                totalAmount = 13_000.toBigDecimal(),
            )

            shouldThrow<InvalidOrderPriceException> {
                command.validate()
            }
        }
    }
}
