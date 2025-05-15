package kr.hhplus.be.server.domain.product

import java.math.BigDecimal

data class ProductPrice(val value: BigDecimal) {

    fun isEqualTo(
        price: BigDecimal,
    ): Boolean =
        this.value.compareTo(price) == 0
}
