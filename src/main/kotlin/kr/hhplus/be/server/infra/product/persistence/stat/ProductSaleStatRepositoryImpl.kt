package kr.hhplus.be.server.infra.product.persistence.stat

import kr.hhplus.be.server.domain.product.stat.ProductSaleStat
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import org.springframework.stereotype.Repository

@Repository
class ProductSaleStatRepositoryImpl(
    private val jpaRepository: ProductSaleStatJpaRepository
) : ProductSaleStatRepository {
    override fun save(stat: ProductSaleStat): ProductSaleStat =
        jpaRepository.save(stat)
}
