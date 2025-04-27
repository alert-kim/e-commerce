package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.application.product.result.ProductsResult
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.command.RecordProductDailySalesCommand
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.util.TimeZone
import org.springframework.stereotype.Service

@Service
class ProductFacade(
    private val service: ProductService,
    private val stockService: StockService,
) {
    fun aggregate(command: AggregateProductDailySalesFacadeCommand) {
        if (command.sales.isEmpty()) return
        val sales = command.sales
            .groupBy {
                Pair(it.productId, it.createdAt.atZone(TimeZone.KSTId).toLocalDate())
            }.map { (key, sales) ->
                val (productId, date) = key
                RecordProductDailySalesCommand.ProductSale(
                    productId = ProductId(productId),
                    date = date,
                    quantity = sales.sumOf { it.quantity }
                )
            }

        service.aggregateProductDailySales(
            RecordProductDailySalesCommand(
                sales = sales
            )
        )
    }

    fun getAllOnSalePaged(page: Int, pageSize: Int): ProductsResult.Paged {
        val products = service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        val productIds = products.content.map { it.id }
        val stocks = stockService.getStocks(productIds)

        return ProductsResult.Paged.from(products, stocks)
    }

    fun getPopularProducts(): ProductsResult.Listed {
        val popularProducts = service.getPopularProducts()
        val productIds = popularProducts.products.map { it.id }
        val stocks = stockService.getStocks(productIds)

        return ProductsResult.Listed.from(popularProducts.products, stocks)
    }
}
