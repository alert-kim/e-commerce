package kr.hhplus.be.server.scheduler.product

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.common.util.TimeZone
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
class ProductSaleStatsScheduler(
    private val productFacade: ProductFacade,
) {
    @Scheduled(cron = "0 10 0 * * *")
    fun aggregateDaily() {
        val today = LocalDate.now(TimeZone.KSTId)

        productFacade.aggregate(
            AggregateProductDailySalesFacadeCommand(
                date = today,
            )
        )
    }
}
