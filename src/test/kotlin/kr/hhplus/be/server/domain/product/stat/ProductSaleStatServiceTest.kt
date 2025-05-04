package kr.hhplus.be.server.domain.product.stat

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.order.OrderSnapshot
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.command.CreateProductSaleStatsCommand
import kr.hhplus.be.server.domain.product.stat.repository.ProductSaleStatRepository
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.util.TimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductSaleStatServiceTest {
    val dailyStatRepository = mockk<ProductDailySaleStatRepository>(relaxed = true)
    val statRepository = mockk<ProductSaleStatRepository>(relaxed = true)
    val service = ProductSaleStatService(statRepository, dailyStatRepository)

    @Nested
    @DisplayName("createStat")
    inner class CreateStats {

        @Test
        @DisplayName("주문 완료 이벤트의 상품 정보를 기반으로 판매 데이터를 저장")
        fun create() {
            val orderProducts = List(2) { OrderMock.product() }
            val event = OrderMock.completedEvent(
                snapshot = OrderSnapshot.from(OrderMock.order(products = orderProducts))
            )
            val today = LocalDate.now(TimeZone.KSTId)

            val command = CreateProductSaleStatsCommand(event)
            service.createStats(command)

            verify {
                orderProducts.forEach {
                    statRepository.save(withArg { stat ->
                        assertThat(stat.productId).isEqualTo(it.productId)
                        assertThat(stat.quantity).isEqualTo(it.quantity)
                        assertThat(stat.date).isEqualTo(today)
                    })
                }
            }
        }
    }

    @Nested
    @DisplayName("createDailyStat")
    inner class CreateDailyStats {

        @Test
        @DisplayName("일간 판매 데이터 집계")
        fun create() {
            val today = LocalDate.now()

            val command = CreateProductDailySaleStatsCommand(today)
            service.createDailyStats(command)

            verify {
                dailyStatRepository.aggregateDailyStatsByDate(today)
            }
        }
    }

    @Nested
    @DisplayName("getPopularProductIds")
    inner class GetPopularProductIds {
        @Test
        fun `getPopularProducts - 인기 상품 조회`() {
            val productIds = List(Arb.int(1..PopularProductsIds.MAX_SIZE).next()) {
                ProductMock.id()
            }
            val sales = productIds.map { ProductMock.dailySale(productId = it) }
            every {
                dailyStatRepository.findTopNProductsByQuantity(
                    startDate = PopularProductsIds.getStartDay(),
                    endDate = PopularProductsIds.getEndDay(),
                    limit = PopularProductsIds.MAX_SIZE,
                )
            } returns sales

            val result = service.getPopularProductIds()

            assertThat(result.value).isEqualTo(productIds)
        }
    }
}
