package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.application.product.result.GetProductsFacadeResult
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.stock.StockService
import org.springframework.stereotype.Service

@Service
class ProductFacade(
    private val productService: ProductService,
    private val productSaleStatService: ProductSaleStatService,
    private val stockService: StockService,
) {

    fun aggregate(command: AggregateProductDailySalesFacadeCommand) {
        productSaleStatService.createDailyStats(CreateProductDailySaleStatsCommand(command.date))
    }

    fun getAllOnSalePaged(page: Int, pageSize: Int): GetProductsFacadeResult.Paged {
        val products =
            productService.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        val productIds = products.content.map { it.id }
        val stocks = stockService.getStocks(productIds)

        return GetProductsFacadeResult.Paged.from(products, stocks)
    }

    fun getPopularProducts(): GetProductsFacadeResult.Listed {
        val popularProducts = productService.getPopularProducts()
        val productIds = popularProducts.products.map { it.id }
        val stocks = stockService.getStocks(productIds)

        return GetProductsFacadeResult.Listed.from(popularProducts.products, stocks)
    }
}
