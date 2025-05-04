package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.util.TimeZone
import java.time.LocalDate

data class PopularProductsIds(
    val value: List<ProductId>,
) {
    init {
        require(value.size <= MAX_SIZE) {
            "인기 상품은 ${MAX_SIZE}개 이하여야 합니다."
        }
    }

    companion object {
        const val MAX_SIZE = 5
        fun getStartDay(): LocalDate = LocalDate.now(TimeZone.KSTId).minusDays(3)
        fun getEndDay(): LocalDate = LocalDate.now(TimeZone.KSTId).minusDays(1)
    }
}
