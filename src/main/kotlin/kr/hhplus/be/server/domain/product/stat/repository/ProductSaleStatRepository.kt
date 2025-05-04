package kr.hhplus.be.server.domain.product.stat.repository

import kr.hhplus.be.server.domain.product.stat.ProductSaleStat

interface ProductSaleStatRepository {
    fun save(stat: ProductSaleStat): ProductSaleStat
}
