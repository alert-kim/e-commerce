package kr.hhplus.be.server.application.order.command

import kr.hhplus.be.server.testutil.mock.OrderCommandMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PlaceOrderProductProcessorCommandTest {

    @Test
    @DisplayName("OrderFacadeCommand.ProductToOrder 와 OrderId에 대한 command 생성 ")
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
