package kr.hhplus.be.server.interfaces.product.event.order

import kr.hhplus.be.server.application.product.ProductRankingFacade
import kr.hhplus.be.server.application.product.ProductSaleStatFacade
import kr.hhplus.be.server.application.product.command.CreateProductSaleStatsFacadeCommand
import kr.hhplus.be.server.application.product.command.UpdateProductRankingFacadeCommand
import kr.hhplus.be.server.domain.order.event.OrderCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductOrderEventListener(
    private val productRankingFacade: ProductRankingFacade,
    private val productSaleStatFacade: ProductSaleStatFacade
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OrderCompletedEvent) {
        logger.info("OrderCompletedEvent 수신: orderId={}", event.orderId.value)

        runCatching {
            productRankingFacade.updateRanking(UpdateProductRankingFacadeCommand(event.order))
        }.onSuccess {
            logger.info("상품 랭킹 업데이트 완료: orderId={}", event.orderId.value)
        }.onFailure { exception ->
            logger.error(
                "상품 랭킹 업데이트 중 오류 발생: orderId={}, error={}",
                event.orderId.value, exception.message
            )
        }

        runCatching {
            productSaleStatFacade.createStats(CreateProductSaleStatsFacadeCommand(event))
        }.onSuccess {
            logger.info("상품 판매 통계 생성 완료: orderId={}", event.orderId.value)
        }.onFailure { exception ->
            logger.error(
                "상품 판매 통계 생성 중 오류 발생: orderId={}, error={}",
                event.orderId.value, exception.message
            )
        }
    }
}
