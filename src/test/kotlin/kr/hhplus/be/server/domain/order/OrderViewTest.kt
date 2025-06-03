package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.exception.InvalidOrderStatusException
import kr.hhplus.be.server.domain.order.exception.RequiredOrderIdException
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderViewTest {

    @Nested
    @DisplayName("생성")
    inner class From {

        @Test
        @DisplayName("주문 정보를 올바르게 반환한다")
        fun returnCorrectOrderInfo() {
            val order = OrderMock.order(
                products = List(3) {
                    OrderMock.product()
                },
            )

            val result = OrderView.from(order)

            assertThat(result.id).isEqualTo(order.id())
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
                assertThat(product.productId).isEqualTo(expect.productId)
                assertThat(product.quantity).isEqualTo(expect.quantity)
                assertThat(product.unitPrice).isEqualByComparingTo(expect.unitPrice)
                assertThat(product.totalPrice).isEqualByComparingTo(expect.totalPrice)
                assertThat(product.createdAt).isEqualTo(expect.createdAt)
            }
        }

        @Test
        @DisplayName("주문 아이디가 null이면 예외가 발생한다")
        fun throwExceptionWhenOrderIdIsNull() {
            val order = OrderMock.order(id = null)

            assertThrows<RequiredOrderIdException> {
                OrderView.from(order)
            }
        }
    }

    @Nested
    @DisplayName("실패 여부 확인")
    inner class IsFailed {

        @Test
        @DisplayName("FAILED 상태인지 확인")
        fun checkIfFailed() {
            val failedOrder = OrderMock.view(status = OrderStatus.FAILED)
            val readyOrder = OrderMock.view(status = OrderStatus.READY)

            assertThat(failedOrder.isFailed()).isTrue()
            assertThat(readyOrder.isFailed()).isFalse()
        }
    }

    @Nested
    @DisplayName("주문 확료 확인")
    inner class CheckCompleted {

        @Test
        @DisplayName("완료 상태인 경우 정상 처리")
        fun completedOrder() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(id = orderId, status = OrderStatus.COMPLETED)
            val orderView = OrderView.from(order)

            val result = orderView.checkCompleted()

            assertThat(result).isEqualTo(orderView)
        }

        @Test
        @DisplayName("완료 상태가 아닐 경우 InvalidOrderStatusException 발생")
        fun notCompletedOrder() {
            val orderId = OrderMock.id()
            val status = OrderStatus.entries.filter { it != OrderStatus.COMPLETED }.random()
            val order = OrderMock.order(id = orderId, status = status)
            val orderView = OrderView.from(order)

            assertThrows<InvalidOrderStatusException> {
                orderView.checkCompleted()
            }
        }
    }

    @Nested
    @DisplayName("주문 완료 시각 조회")
    inner class GetOrNullCompletedAt {

        @Test
        @DisplayName("완료 상태인 경우 정상 완료시각 반환")
        fun completedOrder() {
            val orderId = OrderMock.id()
            val order = OrderMock.order(id = orderId, status = OrderStatus.COMPLETED)
            val orderView = OrderView.from(order)

            val result = orderView.getOrNullCompletedAt()

            assertThat(result).isEqualTo(orderView.updatedAt)
        }

        @Test
        @DisplayName("완료 상태가 아닐 경우 null 반환")
        fun notCompletedOrder() {
            val orderId = OrderMock.id()
            val status = OrderStatus.entries.filter { it != OrderStatus.COMPLETED }.random()
            val order = OrderMock.order(id = orderId, status = status)
            val orderView = OrderView.from(order)

            val result = orderView.getOrNullCompletedAt()

            assertThat(result).isNull()
        }
    }
}
