package kr.hhplus.be.server.domain.product

import jakarta.persistence.Embeddable

@Embeddable
data class ProductId(val value: Long)
