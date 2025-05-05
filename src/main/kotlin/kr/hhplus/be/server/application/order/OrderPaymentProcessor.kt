package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.CancelOrderPaymentProcessorCommand
import kr.hhplus.be.server.application.order.command.PayOrderProcessorCommand
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.annotation.DistributedLock
import kr.hhplus.be.server.domain.balance.BalanceService
import kr.hhplus.be.server.domain.balance.command.CancelBalanceUseCommand
import kr.hhplus.be.server.domain.balance.command.UseBalanceCommand
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PayOrderCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.payment.command.CancelPaymentCommand
import kr.hhplus.be.server.domain.payment.command.PayCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit.MILLISECONDS

@Service
class OrderPaymentProcessor(
    private val balanceService: BalanceService,
    private val orderService: OrderService,
    private val paymentService: PaymentService,
) {

    @DistributedLock(
        keyPrefix = "balance",
        identifier = "#command.userId",
        strategy = LockStrategy.SPIN,
        waitTime = 2_000L,
        leaseTime = 1_500L,
        timeUnit = MILLISECONDS,
    )
    @Transactional
    fun processPayment(command: PayOrderProcessorCommand) {
        val usedAmount = balanceService.use(
            UseBalanceCommand(
                userId = command.userId,
                amount = command.totalAmount,
            )
        )

        val payment = paymentService.pay(
            PayCommand(
                userId = command.userId,
                orderId = command.orderId,
                amount = usedAmount,
            )
        )

        orderService.pay(PayOrderCommand(payment))
    }

    @Transactional
    fun cancelPayment(command: CancelOrderPaymentProcessorCommand) {
        val payment = paymentService.getOrNullByOrderId(command.orderId) ?: return
        payment.checkUser(command.userId)

        paymentService.cancelPay(CancelPaymentCommand(payment.id))

        balanceService.cancelUse(CancelBalanceUseCommand(
                userId = payment.userId,
                amount = payment.amount
            )
        )
    }
}
