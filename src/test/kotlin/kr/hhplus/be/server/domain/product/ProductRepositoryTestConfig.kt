package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.infra.product.ProductJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ProductRepositoryTestConfig {

    @Bean
    fun testProductRepository(productJpaRepository: ProductJpaRepository): TestProductRepository =
        TestProductRepository(productJpaRepository)
}

class TestProductRepository(
    private val productJpaRepository: ProductJpaRepository
) {
    fun save(product: Product): Product {
        return productJpaRepository.save(product)
    }

    fun deleteAll() {
        productJpaRepository.deleteAll()
    }
}
