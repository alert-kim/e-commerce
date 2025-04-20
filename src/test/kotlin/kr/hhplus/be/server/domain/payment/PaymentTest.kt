package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.PaymentMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class PaymentTest {
    @Test
    fun `requireId - id가 null이 아닌 경우 id 반환`() {
        val payment = PaymentMock.payment(id = PaymentMock.id())

        val result = payment.requireId()

        assertThat(result).isEqualTo(payment.id)
    }

    @Test
    fun `requireId - id가 null이면 RequiredPaymentIdException 발생`() {
        val payment = PaymentMock.payment(id = null)

        assertThrows<RequiredPaymentIdException> {
            payment.requireId()
        }
    }

    @Test
    fun `new - 해당 주문, 유저아이디와 금액를 가진, 결제를 생성한다`() {
        val userId = UserMock.id()
        val orderId = OrderMock.id()
        val amount = UsedBalanceAmount(
            balanceId = BalanceMock.id(),
            amount = BalanceAmount.of(1000.toBigDecimal()),
        )

        val payment = Payment.new(
            userId = userId,
            orderId = orderId,
            amount = amount,
        )

        assertAll(
            { assertThat(payment.userId).isEqualTo(userId) },
            { assertThat(payment.orderId).isEqualTo(orderId) },
            { assertThat(payment.amount).isEqualByComparingTo(amount.value) },
        )
    }


}
