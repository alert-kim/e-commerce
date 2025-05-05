package kr.hhplus.be.server.domain.payment

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.payment.command.CancelPaymentCommand
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.exception.NotFoundPaymentException
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

class PaymentServiceTest {
    private lateinit var service: PaymentService

    private val repository = mockk<PaymentRepository>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        service = PaymentService(repository)
    }

    @Nested
    @DisplayName("결제 생성")
    inner class Pay {
        @Test
        @DisplayName("해당 정보로 결제를 생성한다")
        fun pay() {
            val userId = UserMock.id()
            val orderId = OrderMock.id()
            val amount = UsedBalanceAmount(
                balanceId = BalanceMock.id(),
                amount = BalanceAmount.of(2_500.toBigDecimal()),
            )
            val payment = PaymentMock.payment()
            every { repository.save(any()) } returns payment

            val result = service.pay(
                PayCommand(
                    orderId = orderId,
                    userId = userId,
                    amount = amount,
                ),
            )

            assertThat(result).isEqualTo(PaymentView.from(payment))
            verify {
                repository.save(
                    withArg {
                        assertThat(it.orderId).isEqualTo(orderId)
                        assertThat(it.userId).isEqualTo(userId)
                        assertThat(it.amount).isEqualByComparingTo(amount.value)
                    }
                )
            }
        }
    }


    @Nested
    @DisplayName("결제 취소")
    inner class CancelPay {
        @Test
        @DisplayName("해당 결제를 취소한다")
        fun cancel() {
            val payment = mockk<Payment>()
            val paymentId = PaymentMock.id()
            val canceledPayment = mockk<Payment>()
            every { repository.findById(paymentId.value) } returns payment
            every { payment.cancel() } returns canceledPayment

            service.cancelPay(CancelPaymentCommand(paymentId))

            verify { payment.cancel() }
        }

        @Test
        @DisplayName("존재하지 않는 결제 ID로 요청 시 NotFoundPaymentException 발생")
        fun notFoundPayment() {
            val paymentId = PaymentMock.id()
            every { repository.findById(paymentId.value) } returns null

            assertThrows<NotFoundPaymentException> {
                service.cancelPay(CancelPaymentCommand(paymentId))
            }
        }
    }

    @Nested
    @DisplayName("주문 ID로 결제 조회")
    inner class GetOrNullByOrderId {
        @Test
        @DisplayName("주문 ID로 결제 조회")
        fun getOrNullByOrderId() {
            val orderId = OrderMock.id()
            val payment = PaymentMock.payment(orderId = orderId)
            val paymentView = PaymentView.from(payment)
            every { repository.findByOrderId(orderId) } returns payment

            val result = service.getOrNullByOrderId(orderId)

            assertThat(result).isEqualTo(paymentView)
            verify { repository.findByOrderId(orderId) }
        }

        @Test
        @DisplayName("결제가 없으면 null을 반환한다")
        fun getByOrderIdReturnsNull() {
            val orderId = OrderMock.id()
            every { repository.findByOrderId(orderId) } returns null

            val result = service.getOrNullByOrderId(orderId)

            assertThat(result).isNull()
            verify { repository.findByOrderId(orderId) }
        }
    }
}
