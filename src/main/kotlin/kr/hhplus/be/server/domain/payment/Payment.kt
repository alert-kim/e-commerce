package kr.hhplus.be.server.domain.payment

import jakarta.persistence.*
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    val userId: UserId,
    val orderId: OrderId,
    @Column(precision = 20, scale = 2)
    val amount: BigDecimal,
    val createdAt: Instant,
    status: PaymentStatus,
    canceledAt: Instant?,
    updatedAt: Instant,
) {
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    var status: PaymentStatus = status
        private set

    var canceledAt: Instant? = canceledAt
        private set

    var updatedAt: Instant = updatedAt
        private set

    fun id(): PaymentId =
        id?.let { PaymentId(id) } ?: throw RequiredPaymentIdException()

    fun cancel(): Payment {
        if (status == PaymentStatus.CANCELED) {
            return this
        }
        status = PaymentStatus.CANCELED
        canceledAt = Instant.now()
        updatedAt = Instant.now()
        return this
    }

    companion object {
        fun new(
            userId: UserId,
            orderId: OrderId,
            amount: UsedBalanceAmount,
        ): Payment =
            Payment(
                userId = userId,
                orderId = orderId,
                status = PaymentStatus.COMPLETED,
                amount = amount.value.setScale(2, RoundingMode.HALF_UP),
                canceledAt = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
    }
}
