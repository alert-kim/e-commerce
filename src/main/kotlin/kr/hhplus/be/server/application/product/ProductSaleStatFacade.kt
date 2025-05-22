package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.application.product.command.CreateProductSaleStatsFacadeCommand
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import org.springframework.stereotype.Service

@Service
class ProductSaleStatFacade(
    private val productSaleStatService: ProductSaleStatService
) {
    fun createStats(command: CreateProductSaleStatsFacadeCommand) {
        productSaleStatService.createStats(
            CreateProductSaleStatsCommand(
                command.event
            )
        )
    }
}
