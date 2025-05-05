package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderSnapshotTest {

    @Test
    fun `주문 스냅샷 생성`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId)

        val orderSnapshot = OrderSnapshot.from(order)

        assertThat(orderSnapshot.id).isEqualTo(orderId)
        assertThat(orderSnapshot.userId).isEqualTo(order.userId)
        assertThat(orderSnapshot.status).isEqualTo(order.status)
        assertThat(orderSnapshot.originalAmount).isEqualByComparingTo(order.originalAmount)
        assertThat(orderSnapshot.discountAmount).isEqualByComparingTo(order.discountAmount)
        assertThat(orderSnapshot.totalAmount).isEqualByComparingTo(order.totalAmount)
        assertThat(orderSnapshot.couponId).isEqualTo(order.couponId)
        assertThat(orderSnapshot.createdAt).isEqualTo(order.createdAt)
        assertThat(orderSnapshot.updatedAt).isEqualTo(order.updatedAt)
        assertThat(orderSnapshot.orderProducts).hasSize(order.products.size)
        orderSnapshot.orderProducts.forEachIndexed { index, product ->
            val expect = order.products[index]
            assertThat(product.productId).isEqualTo(expect.productId.value)
            assertThat(product.quantity).isEqualTo(expect.quantity)
            assertThat(product.unitPrice).isEqualByComparingTo(expect.unitPrice)
            assertThat(product.totalPrice).isEqualByComparingTo(expect.totalPrice)
            assertThat(product.createdAt).isEqualTo(expect.createdAt)
        }
    }

    @Test
    fun `주문 스냅샷 완료 확인 - 완료 상태인 경우 정상 처리`() {
        val orderId = OrderMock.id()
        val order = OrderMock.order(id = orderId, status = OrderStatus.COMPLETED)
        val orderSnapshot = OrderSnapshot.from(order)

        val result = orderSnapshot.checkCompleted()

        assertThat(result).isEqualTo(orderSnapshot)
    }

    @Test
    fun `주문 스냅샷 완료 확인 - 완료 상태가 아닐 경우 - InvalidOrderStatusException 발생`() {
        val orderId = OrderMock.id()
        val status = OrderStatus.entries.filter { it != OrderStatus.COMPLETED }.random()
        val order = OrderMock.order(id = orderId, status = status)
        val orderSnapshot = OrderSnapshot.from(order)

        assertThrows<InvalidOrderStatusException> {
            orderSnapshot.checkCompleted()
        }
    }
}
