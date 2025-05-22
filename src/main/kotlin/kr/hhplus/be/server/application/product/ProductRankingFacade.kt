package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.command.UpdateProductRankingFacadeCommand
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingService
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import org.springframework.stereotype.Service

@Service
class ProductRankingFacade(
    private val productSaleRankingService: ProductSaleRankingService
) {
    fun updateRanking(command: UpdateProductRankingFacadeCommand) {
        productSaleRankingService.updateRanking(UpdateProductSaleRankingCommand(command.event))
    }
}
