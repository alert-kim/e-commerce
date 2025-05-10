package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProductDailySaleStatJpaRepository : JpaRepository<ProductDailySaleStat, Long> {

    @Query(
        """
        SELECT p 
        FROM ProductDailySaleStat p
        WHERE p.date BETWEEN :startDate AND :endDate
        ORDER BY p.quantity DESC
    """
    )
    fun findAllByDateBetweenOrderByQuantityDesc(startDate: LocalDate, endDate: LocalDate): List<ProductDailySaleStat>

    @Query("""
        SELECT product_id
        FROM product_daily_sale_stats
        WHERE date BETWEEN :startDate AND :endDate
            GROUP BY product_id
        ORDER BY SUM(quantity) DESC
        LIMIT :limit
    """, nativeQuery = true
    )
    fun findTopProductIdsByQuantityBetweenDate(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("limit") limit: Int
    ): List<Long>
}
