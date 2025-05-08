package kr.hhplus.be.server.domain.product.stat

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ProductStatRepositoryTest : RepositoryTest() {

    @Autowired
    private lateinit var repository: ProductSaleStatRepository

    @Nested
    @DisplayName("save")
    inner class Save {
        @Test
        @DisplayName("상품 판매 통계를 저장하고 반환한다")
        fun save() {
            val stat = ProductMock.saleStat(id = null)

            val saved = repository.save(stat)

            shouldNotThrowAny { saved.id() }
            assertThat(saved.productId).isEqualTo(stat.productId)
            assertThat(saved.quantity).isEqualTo(stat.quantity)
            assertThat(saved.date).isEqualTo(stat.date)
            assertThat(saved.createdAt).isEqualTo(stat.createdAt)
        }
    }
}
