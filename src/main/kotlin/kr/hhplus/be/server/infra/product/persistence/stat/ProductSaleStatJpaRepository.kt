package kr.hhplus.be.server.infra.product.persistence.stat

import kr.hhplus.be.server.domain.product.stat.ProductSaleStat
import org.springframework.data.jpa.repository.JpaRepository

interface ProductSaleStatJpaRepository : JpaRepository<ProductSaleStat, Long> {
}
