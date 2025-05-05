package kr.hhplus.be.server.domain.payment

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
