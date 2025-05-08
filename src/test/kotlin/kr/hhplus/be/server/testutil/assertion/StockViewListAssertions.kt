package kr.hhplus.be.server.testutil.assertion

import kr.hhplus.be.server.domain.stock.StockView
import org.assertj.core.api.AbstractAssert

class StockViewListAssert(actual: List<StockView>?) : AbstractAssert<StockViewListAssert, List<StockView>>(actual, StockViewListAssert::class.java) {
    fun isEqualTo(expected: List<StockView>): StockViewListAssert {
        isNotNull
        if (actual.size != expected.size) {
            failWithMessage("Expected StockViewList to have size <%s> but was <%s>", expected.size, actual.size)
            return this
        }

        val mismatchedIndices = actual.mapIndexedNotNull { index, stock ->
            val expectedStock = expected[index]
            if (stock != expectedStock) index to "Expected Stock to <${expected}> but was <${stock}>"
            else null
        }

        if (mismatchedIndices.isNotEmpty()) {
            val mismatchMessages = mismatchedIndices.joinToString("\n") { (index, message) ->
                "  - At index $index: $message"
            }
            failWithMessage("StockViewList is not equal to expected:\n$mismatchMessages")
        }

        return this
    }

    companion object {
        fun assertStockViews(actual: List<StockView>?): StockViewListAssert {
            return StockViewListAssert(actual)
        }
    }
}
