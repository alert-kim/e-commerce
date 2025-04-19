package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderViewTest {
    @Test
    fun `주문 정보를 올바르게 반환한다`() {
        val order = OrderMock.order(
            products = List(3) {
                OrderMock.product()
            },
        )

        val result = OrderView.from(order)

        assertThat(result.id).isEqualTo(order.id)
        assertThat(result.userId).isEqualTo(order.userId)
        assertThat(result.status).isEqualTo(order.status)
        assertThat(result.couponId).isEqualTo(order.couponId)
        assertThat(result.originalAmount).isEqualByComparingTo(order.originalAmount)
        assertThat(result.discountAmount).isEqualByComparingTo(order.discountAmount)
        assertThat(result.totalAmount).isEqualByComparingTo(order.totalAmount)
        assertThat(result.createdAt).isEqualTo(order.createdAt)
        assertThat(result.updatedAt).isEqualTo(order.updatedAt)
        
        assertThat(result.products).hasSize(order.products.size)
        result.products.forEachIndexed { index, product ->
            val expect = order.products[index]
            assertThat(product.orderId).isEqualTo(expect.orderId)
            assertThat(product.productId).isEqualTo(expect.productId)
            assertThat(product.quantity).isEqualTo(expect.quantity)
            assertThat(product.unitPrice).isEqualByComparingTo(expect.unitPrice)
            assertThat(product.totalPrice).isEqualByComparingTo(expect.totalPrice)
            assertThat(product.createdAt).isEqualTo(expect.createdAt)
        }
    }

    @Test
    fun `주문 아이디가 null이면 예외가 발생한다`() {
        val order = OrderMock.order(id = null)

        assertThrows<RequiredOrderIdException> {
            OrderView.from(order)
        }
    }
}
