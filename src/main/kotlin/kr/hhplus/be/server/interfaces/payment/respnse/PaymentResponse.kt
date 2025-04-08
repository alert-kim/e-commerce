package kr.hhplus.be.server.interfaces.payment.respnse

import java.math.BigDecimal

class PaymentResponse(
    val id: Long,
    val userId: Long,
    val orderId: Long,
    val couponId: Long?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
)
