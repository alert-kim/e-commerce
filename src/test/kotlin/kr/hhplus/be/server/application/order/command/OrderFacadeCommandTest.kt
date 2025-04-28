package kr.hhplus.be.server.application.order.command

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.domain.order.exception.InvalidOrderProductQuantityException
import kr.hhplus.be.server.mock.OrderCommandMock
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderFacadeCommandTest {
    @Test
    fun `orderProductIds() 주문 요청 상품의 상품 ID가 반환된다`() {
        val productId = ProductMock.id()
        val productsToOrder = listOf(
            OrderCommandMock.productToOrder(
                productId = productId.value,
            ),
            OrderCommandMock.productToOrder(),
        )
        val command = OrderCommandMock.facade(
            productsToOrder = productsToOrder,
        )

        val result = command.orderProductIds()


        assertThat(result).containsAll(productsToOrder.map { it.productId })
    }

    @Test
    fun `quantityOfProduct() 상품 ID에 해당하는 상품의 수량이 반환된다`() {
        val productId = ProductMock.id()
        val quantity = 2
        val productsToOrder = listOf(
            OrderCommandMock.productToOrder(
                productId = productId.value,
                quantity = quantity,
            ),
            OrderCommandMock.productToOrder(
                quantity = 3,
            ),
        )
        val command = OrderCommandMock.facade(
            productsToOrder = productsToOrder,
        )

        val result = command.quantityOfProduct(productId)

        assertThat(result).isEqualTo(quantity)
    }

    @Test
    fun `validate() 모든 가격 계산이 맞으면 에러가 발생하지 않는다`() {
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
    fun `validate() 주문 상품이 비어 있으면 에러가 발생한다`() {
        val command = OrderCommandMock.facade(
            productsToOrder = emptyList(),
        )

        shouldThrow<InvalidOrderProductQuantityException> {
            command.validate()
        }
    }

    @Test
    fun `validate() 주문 상품의 가격이 합이 총 원 가격과 같지 않으면 에러가 발생한다`() {
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
    fun `validate() 총 원가격이 할인 금액과 총 비용의 합과 다르면(origianl != discount + total) 에러가 발생한다`() {
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
    fun `validate() 주문 상품 중 일부가 수량이 0이면 에러가 발생한다`() {
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
    fun `validate() 주문 상품 중 일부가 단위가격기 0보다 작으면 에러가 발생한다`() {
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
    fun `validate() 주문 상품의 수량 * 단위 가격 != 총가격 이면 에러가 발생한다`() {
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
