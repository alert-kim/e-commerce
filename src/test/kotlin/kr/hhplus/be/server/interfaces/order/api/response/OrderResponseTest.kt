package kr.hhplus.be.server.interfaces.order.api.response

import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.interfaces.order.api.response.OrderResponse
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderResponseTest {

    @Nested
    @DisplayName("응답 변환")
    inner class Convert {
        @Test
        @DisplayName("주문 정보를 응답 형식으로 변환한다")
        fun from() {
            val order = OrderMock.view()
            val result = OrderFacadeResult(order)

            val orderResponse = OrderResponse.from(result)

            val expect = OrderResponse(
                id = order.id.value,
                userId = order.userId.value,
                status = order.status,
                couponId = order.couponId?.value,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                totalAmount = order.totalAmount,
                orderProducts = order.products.map {
                    OrderResponse.OrderProductResponse(
                        productId = it.productId.value,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice,
                    )
                },
                createdAt = order.createdAt,
            )
            assertThat(orderResponse).isEqualTo(expect)
        }
    }
}
