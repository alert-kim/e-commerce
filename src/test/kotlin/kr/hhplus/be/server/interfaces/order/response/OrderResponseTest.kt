package kr.hhplus.be.server.interfaces.order.response

import kr.hhplus.be.server.application.order.result.OrderFacadeResult
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

class OrderResponseTest {
    @Test
    fun `주문에 대한 변환 생성`() {
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
