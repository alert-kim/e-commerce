package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.application.order.command.PlaceOrderProductProcessorCommand
import kr.hhplus.be.server.common.lock.LockStrategy
import kr.hhplus.be.server.common.lock.annotation.DistributedLock
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.order.command.PlaceStockCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.domain.stock.command.AllocateStockCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit.MILLISECONDS

@Service
class OrderProductProcessor(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val stockService: StockService,
) {

    @DistributedLock(
        keyPrefix = "stock",
        identifier = "#command.productId",
        strategy = LockStrategy.SIMPLE,
        waitTime = 2_000,
        leaseTime = 1_500,
        timeUnit = MILLISECONDS,
    )
    @Transactional
    fun placeOrderProduct(command: PlaceOrderProductProcessorCommand) {
        val purchasableProduct = productService.get(command.productId).validatePurchasable(
            price = command.unitPrice
        )
        val stockAllocated = stockService.allocate(
            AllocateStockCommand(
                productId = purchasableProduct.id,
                quantity = command.quantity,
            )
        )

        orderService.placeStock(
            PlaceStockCommand(
                command.orderId,
                purchasableProduct,
                stockAllocated,
            )
        )
    }
}
