package kr.hhplus.be.server.domain.payment

import jakarta.persistence.*
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected val id: Long? = null,
    val userId: UserId,
    val orderId: OrderId,
    val amount: BigDecimal,
    val createdAt: Instant,
) {
    fun id(): PaymentId =
        id ?.let { PaymentId(id) }?: throw RequiredPaymentIdException()

    companion object {
        fun new(
            userId: UserId,
            orderId: OrderId,
            amount: UsedBalanceAmount,
        ): Payment =
            Payment(
                userId = userId,
                orderId = orderId,
                amount = amount.value,
                createdAt = Instant.now(),
            )
    }
}
