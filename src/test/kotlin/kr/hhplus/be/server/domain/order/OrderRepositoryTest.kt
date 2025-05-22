package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.coupon.CouponId
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.product.ProductPrice
import kr.hhplus.be.server.testutil.assertion.OrderAssert.Companion.assertOrder
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("OrderRepository 테스트")
class OrderRepositoryTest @Autowired constructor(
    private val repository: OrderRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("save 메서드")
    inner class SaveMethod {

        @Test
        @DisplayName("(주문 상품이 없는) 주문이 저장되고 ID가 할당됨")
        fun saveOrderWithoutProducts() {
            val order = OrderMock.order(id = null, products = listOf())

            val saved = repository.save(order)

            assertOrder(saved).isEqualTo(order)
        }

        @Test
        @DisplayName("(주문 상품이 있는) 주문이 저장되고 ID가 할당됨")
        fun saveOrderWithProducts() {
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
    }

    @Nested
    @DisplayName("findById 메서드")
    inner class FindByIdMethod {

        @Test
        @DisplayName("ID로 주문을 찾을 수 있음")
        fun findOrderById() {
            val order = OrderMock.order(id = null, products = emptyList(), couponId = CouponId(IdMock.value()))
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
        @DisplayName("존재하지 않는 ID로 조회하면 null 반환")
        fun returnNullForNonExistingId() {
            val nonExistingId = IdMock.value()

            val result = repository.findById(nonExistingId)

            assertThat(result).isNull()
        }
    }
}
