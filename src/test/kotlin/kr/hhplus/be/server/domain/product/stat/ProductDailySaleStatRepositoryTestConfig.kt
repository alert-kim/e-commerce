package kr.hhplus.be.server.domain.product.stat

import kr.hhplus.be.server.infra.product.persistence.ProductDailySaleStatJpaRepository
import kr.hhplus.be.server.infra.product.persistence.stat.ProductSaleStatJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ProductDailySaleStatRepositoryTestConfig {

    @Bean
    fun testProductDailySaleStatRepository(
        statJpaRepository: ProductSaleStatJpaRepository,
        dailyStatJpaRepository: ProductDailySaleStatJpaRepository,
        ): TestProductDailySaleStatRepository =
        TestProductDailySaleStatRepository(dailyStatJpaRepository, statJpaRepository)
}

class TestProductDailySaleStatRepository(
    private val dailyStatJpaRepository: ProductDailySaleStatJpaRepository,
    private val statJpaRepository: ProductSaleStatJpaRepository,
) {
    fun save(sale: ProductDailySaleStat): ProductDailySaleStat {
        return dailyStatJpaRepository.save(sale)
    }

    fun findAll(): List<ProductDailySaleStat> {
        return dailyStatJpaRepository.findAll()
    }

    fun deleteAll() {
        dailyStatJpaRepository.deleteAll()
        statJpaRepository.deleteAll()
    }
}
