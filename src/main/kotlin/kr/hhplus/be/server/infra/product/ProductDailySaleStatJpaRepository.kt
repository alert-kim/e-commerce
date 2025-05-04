package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.ProductDailySaleStat
import kr.hhplus.be.server.domain.product.ProductDailySaleStatId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProductDailySaleStatJpaRepository : JpaRepository<ProductDailySaleStat, ProductDailySaleStatId> {

    @Query(
        """
        SELECT p 
        FROM ProductDailySaleStat p
        WHERE p.id.date BETWEEN :startDate AND :endDate
        ORDER BY p.quantity DESC
    """
    )
    fun findAllByDateBetweenOrderByQuantityDesc(startDate: LocalDate, endDate: LocalDate): List<ProductDailySaleStat>
}
