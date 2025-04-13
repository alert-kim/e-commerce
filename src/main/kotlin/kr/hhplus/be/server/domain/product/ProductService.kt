package kr.hhplus.be.server.domain.product

import kr.hhplus.be.server.domain.common.createPageRequest
import kr.hhplus.be.server.domain.product.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.product.result.AllocatedStockResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val repository: ProductRepository,
) {
    fun allocateStocks(
        command: AllocateStocksCommand
    ): AllocatedStockResult {
        val productMap: Map<Long, Product> = repository
            .findAllByIds(command.needStocks.map { it.productId })
            .associateBy { it.requireId().value }

        val stocks = command.needStocks.map {
            val product = productMap[it.productId]
                ?: throw NotFoundProductException("by id: ${it.productId}")
            val result = product.allocateStock(it.quantity)
            repository.save(product)
            result
        }

        return AllocatedStockResult(
            stocks = stocks,
        )
    }

    fun getAllByStatusOnPaged(status: ProductStatus, page: Int, pageSize: Int): Page<Product> {
        val pageable =
            createPageRequest(page = page, pageSize = pageSize, sort = Sort.by(Sort.Direction.DESC, "createdAt"))
        return repository.findAllByStatus(status, pageable)
    }
}
