package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductDailySaleStatRepositoryImpl(
    private val jpaRepository: ProductDailySaleStatJpaRepository,
    private val customQueryRepository: ProductDailySaleCustomQueryRepository,
) : ProductDailySaleStatRepository {

    override fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<ProductDailySaleStat> =
        jpaRepository.findAllByDateBetweenOrderByQuantityDesc(startDate, endDate).take(limit)


    override fun findTopNProductIdsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<ProductId> =
        jpaRepository.findTopProductIdsByQuantityBetweenDate(startDate, endDate, limit).map { ProductId(it) }

    override fun aggregateDailyStatsByDate(date: LocalDate) {
        customQueryRepository.aggregateDailyStatsByDate(date)
    }
}
