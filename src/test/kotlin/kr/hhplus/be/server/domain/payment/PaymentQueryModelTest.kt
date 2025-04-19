package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.mock.PaymentMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PaymentQueryModelTest {
    @Test
    fun `결제 정보를 올바르게 반환한다`() {
        val payment = PaymentMock.payment()

        val result = PaymentQueryModel.from(payment)

        assertThat(result.id).isEqualTo(payment.id)
        assertThat(result.userId).isEqualTo(payment.userId)
        assertThat(result.orderId).isEqualTo(payment.orderId)
        assertThat(result.amount).isEqualTo(payment.amount.toString())
        assertThat(result.createdAt).isEqualTo(payment.createdAt.toString())
    }
}
