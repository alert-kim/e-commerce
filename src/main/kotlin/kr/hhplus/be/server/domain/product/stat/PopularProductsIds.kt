package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.product.ProductId
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
        const val DAYS_RANGE = 2L

        fun getStartDateFromBaseDate(baseDate: LocalDate): LocalDate =
            baseDate.minusDays(DAYS_RANGE)
    }
}
