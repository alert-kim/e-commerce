package kr.hhplus.be.server.infra.product

import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductDailySaleStatJpaRepository : JpaRepository<ProductDailySaleStat, Long> {
}
