package kr.hhplus.be.server.domain.stock

import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StockViewTest {
    @Test
    fun `from - Stock을 StockView로 변환`() {
        val stock = StockMock.stock()

        val result = StockView.from(stock)

        assertThat(result.id).isEqualTo(stock.id())
        assertThat(result.productId).isEqualTo(stock.productId)
        assertThat(result.quantity).isEqualTo(stock.quantity)
        assertThat(result.createdAt).isEqualTo(stock.createdAt)
        assertThat(result.updatedAt).isEqualTo(stock.updatedAt)
    }
}
