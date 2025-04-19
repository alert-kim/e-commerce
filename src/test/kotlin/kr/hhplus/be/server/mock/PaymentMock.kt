package kr.hhplus.be.server.mock

import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentId
import kr.hhplus.be.server.domain.payment.PaymentView
import kr.hhplus.be.server.domain.user.UserId
import java.math.BigDecimal
import java.time.Instant

object PaymentMock {
    fun id(): PaymentId =
        PaymentId(IdMock.value())

    fun payment(
        id: PaymentId? = id(),
        userId: UserId = UserMock.id(),
        orderId: OrderId = OrderMock.id(),
        amount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
    ): Payment =
        Payment(
            id = id,
            userId = userId,
            orderId = orderId,
            amount = amount,
            createdAt = createdAt,
        )

    fun view(
        id: PaymentId = id(),
        userId: UserId = UserMock.id(),
        orderId: OrderId = OrderMock.id(),
        amount: BigDecimal = BigDecimal.valueOf(1_000),
        createdAt: Instant = Instant.now(),
    ): PaymentView =
        PaymentView(
            id = id,
            userId = userId,
            orderId = orderId,
            amount = amount,
            createdAt = createdAt,
    )
}
