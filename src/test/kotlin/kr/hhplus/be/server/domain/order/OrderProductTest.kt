package kr.hhplus.be.server.domain.order

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderProductIdException
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class OrderProductTest {
    @Test
    fun `id() - id가 null이 아닌 경우 id 반환`() {
        val id = OrderMock.productId()
        val orderProduct = OrderMock.product(id = id)

        val result = orderProduct.id()

        assertThat(result).isEqualTo(id)
    }

    @Test
    fun `id() - id가 null이면 RequiredOrderProductIdException 발생`() {
        val order = OrderMock.product(id = null)

        assertThrows<RequiredOrderProductIdException> {
            order.id()
        }
    }

    @Test
    fun `orderId() - order가 null이 아닌 경우 orderId 반환`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId)
        val orderProduct = OrderMock.product(order = order)

        val result = orderProduct.orderId()

        assertThat(result).isEqualTo(orderId)
    }

    @Test
    fun `orderId() - order가 null이면 RequiredOrderIdException 발생`() {
        val orderProduct = OrderMock.product(order = null)

        assertThrows<RequiredOrderIdException> {
            orderProduct.orderId()
        }
    }

    @Test
    fun `new - 단위가격 * 총 수량 = 총 가격으로 생성`() {
        val orderId = OrderMock.id()
        val productId = ProductMock.id()
        val quantity = Arb.int(1..100).next()
        val unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(100), max = BigDecimal.valueOf(1000)).next()

        val orderProduct = OrderProduct.new(
            order= OrderMock.order(id = orderId),
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice,
        )

        assertAll(
            { assertThat(orderProduct.orderId()).isEqualTo(orderId) },
            { assertThat(orderProduct.productId).isEqualTo(productId) },
            { assertThat(orderProduct.quantity).isEqualTo(quantity) },
            { assertThat(orderProduct.unitPrice).isEqualByComparingTo(unitPrice) },
            { assertThat(orderProduct.totalPrice).isEqualByComparingTo(unitPrice.multiply(BigDecimal(quantity))) },
        )
    }
}
