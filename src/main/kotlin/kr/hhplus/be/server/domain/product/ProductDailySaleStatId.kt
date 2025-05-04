package kr.hhplus.be.server.domain.product

import jakarta.persistence.Embeddable
import java.time.LocalDate

@Embeddable
data class ProductDailySaleStatId(
    val date: LocalDate,
    val productId: ProductId
)
