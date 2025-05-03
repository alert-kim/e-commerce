package kr.hhplus.be.server.application.order

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifyOrder
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class OrderPaymentProcessorTest {

    @InjectMockKs
    private lateinit var processor: OrderPaymentProcessor

    @MockK(relaxed = true)
    private lateinit var balanceService: BalanceService

    @MockK(relaxed = true)
    private lateinit var orderService: OrderService

    @MockK(relaxed = true)
    private lateinit var paymentService: PaymentService

    @Nested
    @DisplayName("주문 결제 처리")
    inner class ProcessPayment {
        @Test
        @DisplayName("주문에 대한 결제를 처리한다")
        fun processPayment() {
            val orderId = OrderMock.id()
            val userId = UserMock.id()
            val totalAmount = BigDecimal.valueOf(10000)
            val usedAmount = UsedBalanceAmount(
                balanceId = BalanceMock.id(),
                amount = BalanceAmount.of(totalAmount),
            )
            val payment = PaymentMock.view(
                orderId = orderId,
                userId = userId,
                amount = usedAmount.amount.value,
            )
            every { balanceService.use(any<UseBalanceCommand>()) } returns usedAmount
            every { paymentService.pay(any<PayCommand>()) } returns payment

            val command = PayOrderProcessorCommand(
                orderId = orderId,
                userId = userId,
                totalAmount = totalAmount
            )
            processor.processPayment(command)

            verifyOrder {
                balanceService.use(
                    UseBalanceCommand(
                        userId = userId,
                        amount = totalAmount
                    )
                )
                paymentService.pay(
                    PayCommand(
                        userId = userId,
                        orderId = orderId,
                        amount = usedAmount,
                    )
                )
                orderService.pay(
                    PayOrderCommand(
                        payment = payment
                    )
                )
            }
        }
    }
}
