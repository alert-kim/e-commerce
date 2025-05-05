package kr.hhplus.be.server.application.order.processor

import io.mockk.*
import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.domain.balance.BalanceAmount
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.CancelBalanceUseCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.CancelPaymentCommand
import kr.hhplus.be.server.domain.payment.command.PayCommand
import kr.hhplus.be.server.domain.payment.exception.NotOwnedPaymentException
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.PaymentMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.junit.jupiter.api.*
import java.math.BigDecimal

class OrderPaymentProcessorTest {

    private lateinit var processor: OrderPaymentProcessor

    private val balanceService = mockk<BalanceService>(relaxed = true)
    private val orderService = mockk<OrderService>(relaxed = true)
    private val paymentService = mockk<PaymentService>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        processor = OrderPaymentProcessor(
            balanceService = balanceService,
            orderService = orderService,
            paymentService = paymentService,
        )
    }

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

    @Nested
    @DisplayName("결제 취소 처리")
    inner class CancelPayment {
        @Test
        @DisplayName("결제를 취소한다")
        fun cancel() {
            val payment = PaymentMock.view()
            every { paymentService.getOrNullByOrderId(payment.orderId) } returns payment

            processor.cancelPayment(CancelOrderPaymentProcessorCommand(payment.orderId, payment.userId))

            verifyOrder {
                paymentService.getOrNullByOrderId(payment.orderId)
                paymentService.cancelPay(CancelPaymentCommand(payment.id))
                balanceService.cancelUse(CancelBalanceUseCommand(payment.userId, payment.amount))
            }
        }

        @Test
        @DisplayName("결제 정보가 없으면 취소 처리 없이 종료한다")
        fun noPayment() {
            every { paymentService.getOrNullByOrderId(any()) } returns null

            processor.cancelPayment(
                CancelOrderPaymentProcessorCommand(
                    OrderMock.id(), UserMock.id()
                )
            )

            verify(exactly = 0) {
                paymentService.cancelPay(any())
                balanceService.cancelUse(any())
            }
        }

        @Test
        @DisplayName("다른 사용자의 결제를 취소하려고 하면 예외가 발생한다")
        fun notOwnedPayment() {
            val paymentOwnerId = UserMock.id(1)
            val requestUserId = UserMock.id(2)
            val payment = PaymentMock.view(
                userId = paymentOwnerId,
            )
            every { paymentService.getOrNullByOrderId(payment.orderId) } returns payment

            assertThrows<NotOwnedPaymentException> {
                processor.cancelPayment(CancelOrderPaymentProcessorCommand(payment.orderId, requestUserId))
            }

            verify(exactly = 0) {
                paymentService.cancelPay(any())
                balanceService.cancelUse(any())
            }
        }
    }
}
