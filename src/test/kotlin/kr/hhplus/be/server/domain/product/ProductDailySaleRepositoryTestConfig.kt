package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import kr.hhplus.be.server.infra.product.ProductDailySaleJpaRepository
import kr.hhplus.be.server.infra.product.ProductJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ProductDailySaleRepositoryTestConfig {

    @Bean
    fun testProductDailySaleRepository(japRepository: ProductDailySaleJpaRepository): TestProductDailySaleRepository =
        TestProductDailySaleRepository(japRepository)
}

class TestProductDailySaleRepository(
    private val jpaRepository: ProductDailySaleJpaRepository
) {
    fun deleteAll() {
        jpaRepository.deleteAll()
    }
}
