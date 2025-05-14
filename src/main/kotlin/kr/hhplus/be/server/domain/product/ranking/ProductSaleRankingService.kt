package kr.hhplus.be.server.domain.product.ranking

import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import kr.hhplus.be.server.domain.product.stat.PopularProductsIds
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ProductSaleRankingService(
    private val repository: ProductSaleRankingRepository,
) {
    fun updateRanking(command: UpdateProductSaleRankingCommand) {
        val soldAt = command.event.completedAt
        val date = LocalDate.ofInstant(soldAt, TimeZone.KSTId)
        command.event.orderProducts.forEach {
            repository.updateRanking(ProductSaleRankingEntry(
                date = date,
                productId = it.productId,
                quantity = it.quantity,
                orderCount = 1,
            ))
        }
    }
}

