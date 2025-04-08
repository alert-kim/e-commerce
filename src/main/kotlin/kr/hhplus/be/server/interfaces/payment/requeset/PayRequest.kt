package kr.hhplus.be.server.interfaces.payment.requeset

import java.math.BigDecimal

class PayRequest(
    val userId: Long,
    val orderId: Long,
    val couponId: Long?,
    val originalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
)
