package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.testutil.mock.OrderCommandMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlaceOrderProductProcessorCommandTest {

    @Nested
    @DisplayName("명령 객체 생성")
    inner class Creation {
        @Test
        @DisplayName("주문 ID와 상품 정보로 명령 객체를 생성한다")
        fun of() {
            val facadeCommand = OrderCommandMock.productToOrder()
            val orderId = OrderMock.id()

            val result = PlaceOrderProductProcessorCommand.of(
                facadeCommand, orderId,
            )

            assertThat(result.orderId).isEqualTo(orderId)
            assertThat(result.productId).isEqualTo(facadeCommand.productId)
            assertThat(result.quantity).isEqualTo(facadeCommand.quantity)
            assertThat(result.unitPrice).isEqualByComparingTo(facadeCommand.unitPrice)
        }
    }
}
