package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductDailySaleStatRepositoryImpl(
    private val customQueryRepository: ProductDailySaleCustomQueryRepository,
) : ProductDailySaleStatRepository {

    override fun aggregateDailyStatsByDate(date: LocalDate) {
        customQueryRepository.aggregateDailyStatsByDate(date)
    }
}
