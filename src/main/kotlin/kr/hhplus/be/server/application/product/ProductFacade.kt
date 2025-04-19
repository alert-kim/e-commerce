package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.domain.product.ProductQueryModel
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.command.RecordProductDailySalesCommand
import kr.hhplus.be.server.util.TimeZone
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductFacade(
    private val service: ProductService,
) {
    fun aggregate(command: AggregateProductDailySalesFacadeCommand) {
        if (command.sales.isEmpty()) return
        val sales = command.sales
            .groupBy {
                Pair(it.productId, it.createdAt.atZone(TimeZone.KSTId).toLocalDate())
            }.map { (key, sales) ->
                val (productId, localDate) = key
                RecordProductDailySalesCommand.ProductSale(
                    productId = productId,
                    localDate = localDate,
                    quantity = sales.sumOf { it.quantity }
                )
            }

        service.aggregateProductDailySales(
            RecordProductDailySalesCommand(
                sales = sales
            )
        )
    }

    fun getAllOnSalePaged(page: Int, pageSize: Int): Page<ProductQueryModel> {
        val products = service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        return products.map { product ->
            ProductQueryModel.from(product)
        }
    }
}
