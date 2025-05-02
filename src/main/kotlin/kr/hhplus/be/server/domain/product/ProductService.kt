package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.common.createPageRequest
import kr.hhplus.be.server.domain.product.command.RecordProductDailySalesCommand
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val repository: ProductRepository,
    private val dailySaleRepository: ProductDailySaleRepository,
) {
    fun aggregateProductDailySales(command: RecordProductDailySalesCommand) {
        command.sales.forEach { newSale ->
            val sale = dailySaleRepository.findById(ProductDailySaleId(
                productId = newSale.productId,
                date = newSale.date,
            ))
            when (sale == null) {
                true -> {
                    val sale = ProductDailySale.new(
                        productId = newSale.productId,
                        date = newSale.date,
                        quantity = newSale.quantity,
                    )
                    dailySaleRepository.save(sale)
                }

                false -> {
                    sale.addQuantity(newSale.quantity)
                }
            }
        }
    }

    fun getAllByStatusOnPaged(status: ProductStatus, page: Int, pageSize: Int): Page<ProductView> {
        val pageable =
            createPageRequest(page = page, pageSize = pageSize, sort = Sort.by(Sort.Direction.DESC, "createdAt"))
        return repository.findAllByStatusOrderByCreatedAtDesc(status, pageable)
            .map { ProductView.from(it) }
    }

    fun getAllByIds(ids: List<Long>): ProductsView =
        repository.findAllByIds(ids).map { ProductView.from(it) }.let { ProductsView(it) }

    fun getPopularProducts(): PopularProductsView {
        val popularProductIds = dailySaleRepository.findTopNProductsByQuantity(
            startDate = PopularProducts.getStartDay(),
            endDate = PopularProducts.getEndDay(),
            limit = PopularProducts.MAX_SIZE,
        ).map { it.productId.value }
        val products = repository.findAllByIds(popularProductIds)
        return PopularProductsView(products.map { ProductView.from(it) })
    }
}

