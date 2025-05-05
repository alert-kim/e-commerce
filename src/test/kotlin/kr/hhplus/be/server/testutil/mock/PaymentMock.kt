package kr.hhplus.be.server.testutil.mock

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentId
import kr.hhplus.be.server.domain.payment.PaymentStatus
import kr.hhplus.be.server.domain.payment.PaymentView
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object PaymentMock {
    fun id(): PaymentId =
        PaymentId(IdMock.value())

    fun payment(
        id: Long? = IdMock.value(),
        userId: UserId = UserMock.id(),
        status: PaymentStatus = PaymentStatus.COMPLETED,
        orderId: OrderId = OrderMock.id(),
        amount: BigDecimal = BigDecimal.valueOf(1_000),
        canceledAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Payment =
        Payment(
            id = id,
            userId = userId,
            status = status,
            orderId = orderId,
            amount = amount,
            canceledAt = canceledAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun view(
        id: PaymentId = id(),
        userId: UserId = UserMock.id(),
        status: PaymentStatus = PaymentStatus.COMPLETED,
        orderId: OrderId = OrderMock.id(),
        amount: BigDecimal = BigDecimal.valueOf(1_000),
        canceledAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): PaymentView =
        PaymentView(
            id = id,
            userId = userId,
            status = status,
            orderId = orderId,
            amount = amount,
            canceledAt = canceledAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
