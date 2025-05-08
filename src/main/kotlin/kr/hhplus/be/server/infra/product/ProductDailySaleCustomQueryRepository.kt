package kr.hhplus.be.server.infra.product

import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ProductDailySaleCustomQueryRepository(
    private val jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate,
) {
    fun aggregateDailyStatsByDate(date: LocalDate): Int {
        val sql = """
            INSERT INTO product_daily_sale_stats (date, product_id, quantity, created_at, updated_at)
            SELECT 
                p.date,
                p.product_id,
                SUM(p.quantity) as quantity,
                NOW() as created_at,
                NOW() as updated_at
            FROM product_sale_stats p
            WHERE p.date = ?
            GROUP BY p.product_id, p.date
            ON DUPLICATE KEY UPDATE
                quantity = VALUES(quantity),
                updated_at = NOW()
        """
        return jdbcTemplate.update(sql, date)
    }
}
