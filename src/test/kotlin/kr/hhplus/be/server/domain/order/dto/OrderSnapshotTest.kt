package kr.hhplus.be.server.domain.order.dto

import kr.hhplus.be.server.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrderSnapshotTest {

    @Test
    fun `주문 스냅샷 생성`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId)

        val orderSnapshot = OrderSnapshot.from(order)

        assertThat(orderSnapshot.id).isEqualTo(orderId.value)
        assertThat(orderSnapshot.userId).isEqualTo(order.userId.value)
        assertThat(orderSnapshot.status).isEqualTo(order.status)
        assertThat(orderSnapshot.originalAmount).isEqualByComparingTo(order.originalAmount)
        assertThat(orderSnapshot.discountAmount).isEqualByComparingTo(order.discountAmount)
        assertThat(orderSnapshot.totalAmount).isEqualByComparingTo(order.totalAmount)
        assertThat(orderSnapshot.couponId).isEqualTo(order.couponId?.value)
        assertThat(orderSnapshot.createdAt).isEqualTo(order.createdAt)
        assertThat(orderSnapshot.updatedAt).isEqualTo(order.updatedAt)
        assertThat(orderSnapshot.orderProducts).hasSize(order.products.size)
        orderSnapshot.orderProducts.forEachIndexed { index, product ->
            val expect = order.products[index]
            assertThat(product.orderId).isEqualTo(expect.orderId.value)
            assertThat(product.productId).isEqualTo(expect.productId.value)
            assertThat(product.quantity).isEqualTo(expect.quantity)
            assertThat(product.unitPrice).isEqualByComparingTo(expect.unitPrice)
            assertThat(product.totalPrice).isEqualByComparingTo(expect.totalPrice)
            assertThat(product.createdAt).isEqualTo(expect.createdAt)
        }
    }
}
