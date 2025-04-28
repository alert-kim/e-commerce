package kr.hhplus.be.server.domain.order

import jakarta.persistence.Embeddable

@Embeddable
data class OrderId(val value: Long)
