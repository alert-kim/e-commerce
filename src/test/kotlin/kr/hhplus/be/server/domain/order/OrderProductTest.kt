package kr.hhplus.be.server.domain.order

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.math.min

class OrderProductTest {
    @Test
    fun `new - 단위가격 * 총 수량 = 총 가격으로 생성`() {
        val orderId = OrderMock.id()
        val productId = ProductMock.id()
        val quantity = Arb.int(1..100).next()
        val unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(100), max = BigDecimal.valueOf(1000)).next()

        val orderProduct = OrderProduct.new(
            orderId = orderId,
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice,
        )

        assertAll(
            { assertThat(orderProduct.orderId).isEqualTo(orderId) },
            { assertThat(orderProduct.productId).isEqualTo(productId) },
            { assertThat(orderProduct.quantity).isEqualTo(quantity) },
            { assertThat(orderProduct.unitPrice).isEqualByComparingTo(unitPrice) },
            { assertThat(orderProduct.totalPrice).isEqualByComparingTo(unitPrice.multiply(BigDecimal(quantity))) },
        )
    }
}
