package kr.hhplus.be.server.domain.product.ranking

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.order.OrderSnapshot
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import kr.hhplus.be.server.domain.product.ranking.repository.UpdateProductSaleRankingCommand
import kr.hhplus.be.server.domain.product.stat.PopularProductsIds
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProductSaleRankingServiceTest {
    val repository = mockk<ProductSaleRankingRepository>(relaxed = true)
    val service = ProductSaleRankingService(repository)

    @Nested
    inner class UpdateRanking {

        @Test
        @DisplayName("주문 완료 이벤트의 상품 정보를 기반으로 판매 랭킹 업데이트")
        fun create() {
            val orderProducts = List(2) { OrderMock.product() }
            val event = OrderMock.completedEvent(
                snapshot = OrderSnapshot.from(OrderMock.order(products = orderProducts))
            )
            val date = LocalDate.ofInstant(event.completedAt, TimeZone.KSTId)

            val command = UpdateProductSaleRankingCommand(event)
            service.updateRanking(command)

            verify {
                orderProducts.forEach {
                    repository.updateRanking(
                        ProductSaleRankingEntry(
                            productId = it.productId,
                            quantity = it.quantity,
                            date = date,
                            orderCount = 1,
                        )
                    )
                }
            }
        }
    }

    @Nested
    inner class GetPopularProductIds {

        @Test
        @DisplayName("기준 일자의 인기 상품 ID 목록을 조회")
        fun getTodayPopularProductIds() {
            val productIds = List(Arb.int(1..PopularProductsIds.MAX_SIZE).next()) {
                ProductMock.id()
            }
            val baseDate = LocalDate.now(TimeZone.KSTId)
            val startDate = PopularProductsIds.getStartDateFromBaseDate(baseDate)
            every {
                repository.findTopNProductIds(
                    startDate = startDate,
                    endDate = baseDate,
                    limit = PopularProductsIds.MAX_SIZE,
                )
            } returns productIds

            val result = service.getPopularProductIds()

            assertThat(result.value).isEqualTo(productIds)
        }
    }
}
