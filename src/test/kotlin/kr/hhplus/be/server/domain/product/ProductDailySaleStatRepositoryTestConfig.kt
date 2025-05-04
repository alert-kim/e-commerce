package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infra.product.ProductDailySaleStatJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ProductDailySaleStatRepositoryTestConfig {

    @Bean
    fun testProductDailySaleStatRepository(japRepository: ProductDailySaleStatJpaRepository): TestProductDailySaleStatRepository =
        TestProductDailySaleStatRepository(japRepository)
}

class TestProductDailySaleStatRepository(
    private val jpaRepository: ProductDailySaleStatJpaRepository
) {
    fun save(sale: ProductDailySaleStat): ProductDailySaleStat {
        return jpaRepository.save(sale)
    }

    fun deleteAll() {
        jpaRepository.deleteAll()
    }
}
