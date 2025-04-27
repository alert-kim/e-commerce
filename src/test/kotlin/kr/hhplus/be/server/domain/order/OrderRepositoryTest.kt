package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.mock.IdMock
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.util.OrderAssert.Companion.assertOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OrderRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: OrderRepository

    @Test
    fun `save - (주문 상품이 없는)주문이 저장되고 ID가 할당됨`() {
        val order = OrderMock.order(id = null, products = listOf())

        val saved = repository.save(order)

        assertOrder(saved).isEqualTo(order)
    }

    @Test
    fun `save - (주문 상품이 있는)주문이 저장되고 ID가 할당됨`() {
        val order = OrderMock.order(id = null, products = emptyList())
        val productId = ProductMock.id()
        order.placeStock(
            productId = productId,
            quantity = 2,
            unitPrice = ProductPrice(1000.toBigDecimal()),
        )
        val saved = repository.save(order)

        assertThat(saved.products).hasSize(1)
        assertThat(saved.products[0].productId).isEqualTo(productId)
    }

    @Test
    fun `findById - ID로 주문을 찾을 수 있음`() {
        val order = OrderMock.order(id = null, products = emptyList())
        order.placeStock(
            productId = ProductMock.id(),
            quantity = 2,
            unitPrice = ProductPrice(1000.toBigDecimal()),
        )
        val saved = repository.save(order)

        val found = repository.findById(saved.id().value)

        assertOrder(found).isEqualTo(saved)
    }

    @Test
    fun `findById - 존재하지 않는 ID로 조회하면 null 반환`() {
        val nonExistingId = IdMock.value()

        val result = repository.findById(nonExistingId)

        assertThat(result).isNull()
    }
}
