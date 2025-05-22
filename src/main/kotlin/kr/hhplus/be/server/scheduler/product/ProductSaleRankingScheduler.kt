package kr.hhplus.be.server.scheduler.product

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.RenewPopularProductFacadeCommand
import kr.hhplus.be.server.common.util.TimeZone
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductSaleRankingScheduler(
    private val productFacade: ProductFacade,
) {
    @Scheduled(cron = "0 */5 0 * * *")
    fun aggregate() {
        productFacade.renew(
            RenewPopularProductFacadeCommand(date = LocalDate.now(TimeZone.KSTId))
        )
    }
}
