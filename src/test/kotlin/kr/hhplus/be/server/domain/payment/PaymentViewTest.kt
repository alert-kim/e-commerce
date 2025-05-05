package kr.hhplus.be.server.domain.payment

import kr.hhplus.be.server.domain.payment.exception.NotOwnedPaymentException
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentViewTest {

    @Nested
    @DisplayName("from")
    inner class From {
        @Test
        @DisplayName("결제 정보를 올바르게 변환한다")
        fun from() {
            val payment = PaymentMock.payment()

            val result = PaymentView.from(payment)

            assertThat(result.id).isEqualTo(payment.id())
            assertThat(result.userId).isEqualTo(payment.userId)
            assertThat(result.orderId).isEqualTo(payment.orderId)
            assertThat(result.amount).isEqualByComparingTo(payment.amount)
            assertThat(result.createdAt).isEqualTo(payment.createdAt)
        }
    }

    @Nested
    @DisplayName("사용자 확인")
    inner class CheckUser {
        @Test
        @DisplayName("동일한 사용자이면 자신을 반환")
        fun sameUser() {
            val payment = PaymentMock.view()

            val result = payment.checkUser(payment.userId)

            assertThat(result).isSameAs(payment)
        }

        @Test
        @DisplayName("다른 사용자이면 NotOwnedPaymentException이 발생한다")
        fun differentUser() {
            val ownerId = UserMock.id(1)
            val userId = UserMock.id(2)
            val payment = PaymentMock.view(
                userId = ownerId,
            )

            assertThrows<NotOwnedPaymentException> {
                payment.checkUser(userId)
            }
        }
    }
}
