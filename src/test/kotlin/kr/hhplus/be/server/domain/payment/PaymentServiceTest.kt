package kr.hhplus.be.server.domain.payment

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class PaymentServiceTest {
    @InjectMockKs
    private lateinit var service: PaymentService

    @MockK(relaxed = true)
    private lateinit var repository: PaymentRepository

    @Test
    fun `pay - 해당 정보로 결제를 생성한다`() {
        val userId = UserMock.id()
        val orderId = OrderMock.id()
        val amount = BigDecimal.valueOf(2_500)

        val result = service.pay(
            PayCommand(
                orderId = orderId,
                userId = userId,
                amount = amount,
            ),
        )

        verify {
            repository.save(
                withArg {
                    assertThat(it.orderId).isEqualTo(orderId)
                    assertThat(it.userId).isEqualTo(userId)
                    assertThat(it.amount).isEqualByComparingTo(amount)
                }
            )
        }
    }
}
