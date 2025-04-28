package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.ProductDailySale
import kr.hhplus.be.server.domain.product.ProductDailySaleId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProductDailySaleJpaRepository : JpaRepository<ProductDailySale, ProductDailySaleId> {

    @Query("""
        SELECT p FROM ProductDailySale p
        WHERE p.id.date BETWEEN :startDate AND :endDate
        ORDER BY p.quantity DESC
    """)
    fun findAllByDateBetweenOrderByQuantityDesc(startDate: LocalDate, endDate: LocalDate): List<ProductDailySale>
}
