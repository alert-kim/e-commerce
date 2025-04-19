package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.ProductDailySale
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductDailySaleRepositoryImpl : ProductDailySaleRepository {
    override fun findByProductIdAndDate(productId: ProductId, date: LocalDate): ProductDailySale? {
        TODO("Not yet implemented")
    }

    override fun findTopNProductsByQuantity(
        startDate: LocalDate,
        endDate: LocalDate,
        limit: Int
    ): List<ProductDailySale> {
        TODO("Not yet implemented")
    }

    override fun save(sale: ProductDailySale) {
        TODO("Not yet implemented")
    }

    override fun update(sale: ProductDailySale) {
        TODO("Not yet implemented")
    }
}
