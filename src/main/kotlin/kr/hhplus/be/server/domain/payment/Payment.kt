package kr.hhplus.be.server.domain.payment

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.balance.result.UsedBalanceAmount
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.exception.RequiredPaymentIdException
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@Entity
@Table(
    name = "payments",
    indexes = [
        Index(name = "payment_idx_order", columnList = "order_id"),
    ]
)
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null,
    @Column(nullable = false)
    val userId: UserId,
    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "order_id"))
    @Column(nullable = false)
    val orderId: OrderId,
    @Column(precision = 20, scale = 2, nullable = false)
    val amount: BigDecimal,
    @Column(nullable = false)
    val createdAt: Instant,
    status: PaymentStatus,
    canceledAt: Instant?,
    updatedAt: Instant,
) {
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)", nullable = false)
    var status: PaymentStatus = status
        private set

    var canceledAt: Instant? = canceledAt
        private set

    @Column(nullable = false)
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
