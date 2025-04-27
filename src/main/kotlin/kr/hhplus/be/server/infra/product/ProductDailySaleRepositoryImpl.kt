package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.ProductDailySale
import kr.hhplus.be.server.domain.product.ProductDailySaleId
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductDailySaleRepositoryImpl(
    private val jpaRepository: ProductJpaRepository
) : ProductDailySaleRepository {
    override fun findById(id: ProductDailySaleId): ProductDailySale? {
        return jpaRepository.findById(id).orElse(null)
    }

    override fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<ProductDailySale> =
        jpaRepository.findAllByDateBetweenOrderByQuantityDesc(startDate, endDate).take(limit)

    override fun save(sale: ProductDailySale) =
        jpaRepository.save(sale)
}
