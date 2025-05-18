package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

@DisplayName("Payment 테스트")
class PaymentTest {

    @Nested
    @DisplayName("id()")
    inner class Id {

        @Test
        @DisplayName("id가 null이 아니면 반환")
        fun id() {
            val payment = PaymentMock.payment(id = 1L)

            val result = payment.id()

            assertThat(result.value).isEqualTo(1L)
        }

        @Test
        @DisplayName("id가 null이면 예외 발생")
        fun idIsNull() {
            val payment = PaymentMock.payment(id = null)

            assertThrows<RequiredPaymentIdException> {
                payment.id()
            }
        }
    }

    @Nested
    @DisplayName("new 메서드")
    inner class New {

        @Test
        @DisplayName("주문, 유저 ID, 금액으로 결제 생성")
        fun new() {
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
                { assertThat(payment.status).isEqualTo(PaymentStatus.COMPLETED) },
                { assertThat(payment.canceledAt).isNull() },
            )
        }
    }
}
