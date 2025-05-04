package kr.hhplus.be.server.scheduler.order

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class OrderProductStatsScheduler(
    private val productFacade: ProductFacade,
) {
    @Scheduled(cron = "0 10 0 * * *")
    fun aggregateDaily() {
        val today = LocalDate.now()
        productFacade.aggregate(
            AggregateProductDailySalesFacadeCommand(
                date = today,
            )
        )
    }
}
