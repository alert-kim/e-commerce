package kr.hhplus.be.server.domain.order

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kr.hhplus.be.server.domain.order.exception.InvalidOrderPriceException
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderSheetTest {

    @Test
    fun `주문서 상품 가격 검증 - 상품의 단위 가격 & 각 상품의 총 가격 검증`() {
        val stocks = List(10) {
            ProductMock.stockAllocated(
                quantity = Arb.int(1..10).next(),
                unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(1000), max = BigDecimal.valueOf(10000)).next(),
            )
        }
        val sheet = OrderMock.orderSheet(
            orderProducts = stocks.map { stock ->
                OrderSheet.OrderProduct(
                    productId = stock.productId.value,
                    quantity = stock.quantity,
                    unitPrice = stock.unitPrice,
                    totalPrice = stock.unitPrice.multiply(BigDecimal(stock.quantity)),
                )
            },
        )

        shouldNotThrowAny {
            sheet.verifyProductPrice(stocks)
        }
    }

    @Test
    fun `주문서 상품 가격 검증 - 상품과 주문서의 상품 순서가 다르다면 에러`() {
        val stocks = List(10) {
            ProductMock.stockAllocated(
                quantity = Arb.int(1..10).next(),
                unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(1000), max = BigDecimal.valueOf(10000)).next(),
            )
        }
        val sheet = OrderMock.orderSheet(
            orderProducts = stocks.map { stock ->
                OrderSheet.OrderProduct(
                    productId = stock.productId.value,
                    quantity = stock.quantity,
                    unitPrice = stock.unitPrice,
                    totalPrice = stock.unitPrice.multiply(BigDecimal(stock.quantity)),
                )
            }.reversed(),
        )

        shouldThrow<IllegalArgumentException> {
            sheet.verifyProductPrice(stocks)
        }
    }

    @Test
    fun `주문서 상품 가격 검증 - 상품의 단위 가격이 다르면 에러`() {
        val stocks = List(10) {
            ProductMock.stockAllocated(
                quantity = Arb.int(1..10).next(),
                unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(1000), max = BigDecimal.valueOf(10000)).next(),
            )
        }
        val sheet = OrderMock.orderSheet(
            orderProducts = stocks.mapIndexed { index, stock ->
                OrderSheet.OrderProduct(
                    productId = stock.productId.value,
                    quantity = stock.quantity,
                    unitPrice = if(index == 0) stock.unitPrice else stock.unitPrice.add(BigDecimal.valueOf(100)),
                    totalPrice = stock.unitPrice.multiply(BigDecimal(stock.quantity)),
                )
            },
        )

        shouldThrow<InvalidOrderPriceException> {
            sheet.verifyProductPrice(stocks)
        }
    }

    @Test
    fun `주문서 상품 가격 검증 - 상품의 총 가격이 다르면 에러`() {
        val stocks = List(10) {
            ProductMock.stockAllocated(
                quantity = Arb.int(1..10).next(),
                unitPrice = Arb.bigDecimal(min = BigDecimal.valueOf(1000), max = BigDecimal.valueOf(10000)).next(),
            )
        }
        val sheet = OrderMock.orderSheet(
            orderProducts = stocks.mapIndexed { index, stock ->
                OrderSheet.OrderProduct(
                    productId = stock.productId.value,
                    quantity = stock.quantity,
                    unitPrice = stock.unitPrice,
                    totalPrice = run {
                        val totalPrice = stock.unitPrice.multiply(BigDecimal(stock.quantity))
                        if (index == 0 ) totalPrice else totalPrice.add(BigDecimal.valueOf(100))
                    }
                )
            },
        )

        shouldThrow<InvalidOrderPriceException> {
            sheet.verifyProductPrice(stocks)
        }
    }
}
