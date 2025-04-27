package kr.hhplus.be.server.domain.product

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.time.Instant
import java.time.LocalDate

class ProductDailySaleTest {
    @Test
    fun `new - 상품 일일 판매 데이터 생성`() {
        val date = LocalDate.now()
        val productId = ProductId(3L)
        val quantity = Arb.int(2..4).next()

        val result = ProductDailySale.new(
            date = date,
            productId = productId,
            quantity = quantity,
        )

        assertAll(
            { assert(result.id.date == date) },
            { assert(result.id.productId == productId) },
            { assert(result.quantity == quantity) },
        )
    }

    @Test
    fun `addQuantity - 상품 일일 판매 데이터 수량 추가`() {
        val originalQuantity = 3
        val originalUpdatedAt = Instant.now()
        val quantity = Arb.int(2..4).next()
        val sale = ProductMock.dailySale(quantity = originalQuantity, updatedAt = originalUpdatedAt)

        sale.addQuantity(
            quantity = quantity,
        )

        assertAll(
            { assertThat(sale.quantity).isEqualTo(originalQuantity + quantity) },
            { assertThat(sale.updatedAt).isAfter(originalUpdatedAt) },
        )
    }
}
