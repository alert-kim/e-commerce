package kr.hhplus.be.server.domain.product.ranking

import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import kr.hhplus.be.server.testutil.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDate


@Isolated
@Import(ProductSaleRankingRepositoryTestConfig::class)
@SpringBootTest
class ProductSaleRankingRepositoryTest @Autowired constructor(
    private val repository: ProductSaleRankingRepository,
    private val testRepository: TestSaleRankingRedisRepository
) {

    @BeforeEach
    fun setup() {
        testRepository.deleteAll()
    }

    @Nested
    @DisplayName("랭킹 업데이트")
    inner class UpdateRanking {
        @Test
        @DisplayName("판매수량과 주문수로 랭킹 업데이트 성공")
        fun success() {
            // given
            val date = LocalDate.now()
            val productId1 = ProductMock.id()
            val productId2 = ProductMock.id()

            // when
            // 판매수량 : product1 > product2
            // 판매수량 + 주문수 : product1 < product2
            repository.updateRanking(ProductSaleRankingEntry(
                date = date,
                productId = productId1,
                quantity = 5,
                orderCount = 1
            ))
            repository.updateRanking(ProductSaleRankingEntry(
                date = date,
                productId = productId2,
                quantity = 2,
                orderCount = 5,
            ))

            // then
            val result = repository.renewRanking(date, date, 10)
            assertThat(result).isEqualTo(listOf(productId2, productId1))
        }

        @Test
        @DisplayName("동일 날짜에 대한 점수는 누적")
        fun accumulate() {
            // given
            val date = LocalDate.now()
            val productId1 = ProductId(1L)
            val productId2 =  ProductId(2L)

            // when
            // 단건 : product2 > product1
            // 동일 날짜 누적 : product1 > product2
            // 다른 날짜 까지 누적: product2 > product1
            val entry1 = ProductSaleRankingEntry(
                date = date,
                productId = productId1,
                quantity = 1,
                orderCount = 1
            )
            val entry2 = ProductSaleRankingEntry(
                date = date,
                productId = productId1,
                quantity = 1,
                orderCount = 1
            )
            val entry3 = ProductSaleRankingEntry(
                date = date,
                productId = productId2,
                quantity = 2,
                orderCount = 1
            )
            val entry4 = ProductSaleRankingEntry(
                date = date.plusDays(1),
                productId = productId2,
                quantity = 2,
                orderCount = 1
            )
            repository.updateRanking(entry1)
            repository.updateRanking(entry2)
            repository.updateRanking(entry3)
            repository.updateRanking(entry4)

            // then
            val result = repository.renewRanking(date, date.plusDays(1), 10)
            assertThat(result).isEqualTo(listOf(productId2, productId1))
        }
    }

    @Nested
    @DisplayName("랭킹 갱신")
    inner class RenewRanking {
        @Test
        @DisplayName("판매수량과 주문수로 랭킹 갱신 성공")
        fun renew() {
            // given
            val startDate = LocalDate.now().minusDays(2)
            val middleDate = LocalDate.now().minusDays(1)
            val endDate = LocalDate.now()
            val product1Id = ProductId(1L)
            val product2Id = ProductId(2L)
            val product3Id = ProductId(3L)
            // 예상 랭킹 product1 > product2 > product3
            val product1Entry1 = ProductSaleRankingEntry(
                date = startDate,
                productId = product1Id,
                quantity = 1,
                orderCount = 1
            )
            val product1Entry2 = ProductSaleRankingEntry(
                date = endDate,
                productId = product1Id,
                quantity = 10,
                orderCount = 12
            )
            val product2Entry1 = ProductSaleRankingEntry(
                date = middleDate,
                productId = product2Id,
                quantity = 2,
                orderCount = 3,
            )
            val product2Entry2 = ProductSaleRankingEntry(
                date = endDate,
                productId = product2Id,
                quantity = 2,
                orderCount = 3,
            )
            val product3Entry1 = ProductSaleRankingEntry(
                date = endDate,
                productId = product3Id,
                quantity = 4,
                orderCount = 3,
            )
            repository.updateRanking(product1Entry1)
            repository.updateRanking(product1Entry2)
            repository.updateRanking(product2Entry1)
            repository.updateRanking(product2Entry2)
            repository.updateRanking(product3Entry1)

            // when
            val result = repository.renewRanking(startDate, endDate, 10)

            // then
            assertThat(result).isEqualTo(listOf(product1Id, product2Id, product3Id))
        }

        @Test
        @DisplayName("수량이 동일한 경우 주문 수로 랭킹 결정")
        fun sameQuantity() {
            val startDate = LocalDate.now().minusDays(2)
            val endDate = LocalDate.now()
            val productId1 = ProductId(1L)
            val productId2 = ProductId(2L)
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = startDate,
                    productId = productId1,
                    quantity = 3,
                    orderCount = 1
                )
            )
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = startDate,
                    productId = productId2,
                    quantity = 3,
                    orderCount = 2,
                )
            )

            val result = repository.renewRanking(startDate, endDate, 10)
            assertThat(result).isEqualTo(listOf(productId2, productId1))
        }

        @Test
        @DisplayName("이전 갱신 후 추가된 점수를 반영하여 랭킹 변경")
        fun renewConsideringAdded() {
            // given
            val startDate = LocalDate.now().minusDays(2)
            val endDate = LocalDate.now()
            val productId1 = ProductId(1L)
            val productId2 = ProductId(2L)
            // 기존 점수 : product1 > product2
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = startDate,
                    productId = productId1,
                    quantity = 3,
                    orderCount = 1
                )
            )
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = startDate,
                    productId = productId2,
                    quantity = 2,
                    orderCount = 1
                )
            )
            val initialResult = repository.renewRanking(startDate, endDate, 10)
            require(initialResult == listOf(productId1, productId2))
            // 추가 점수 업데이트: product2 > product1
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = endDate,
                    productId = productId2,
                    quantity = 5,
                    orderCount = 2
                )
            )

            // when
            val result = repository.renewRanking(startDate, endDate, 10)

            // then
            assertThat(result).isEqualTo(listOf(productId2, productId1))
        }

        @Test
        @DisplayName("갱신된 랭킹이 없을 경우 빈 리스트 반환")
        fun empty() {
            // given
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()

            // when
            val result = repository.renewRanking(startDate, endDate, 10)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("limit 만큼의 랭킹만 반환")
        fun limit() {
            // given
            val date = LocalDate.now()
            for (i in 1..5) {
                repository.updateRanking(
                    ProductSaleRankingEntry(
                        date = date,
                        productId = ProductId(i.toLong()),
                        quantity = i * 2,
                        orderCount = i
                    )
                )
            }
            val limit = 3

            // when
            val result = repository.renewRanking(date, date, limit)

            // then
            assertThat(result).hasSize(limit)
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦을 경우 예외 발생")
        fun invalidDate() {
            // given
            val startDate = LocalDate.now()
            val endDate = LocalDate.now().minusDays(1)

            // when&then
            assertThrows<Exception> {
                repository.renewRanking(startDate, endDate, 10)
            }
        }
    }

    @Nested
    @DisplayName("랭킹 조회")
    inner class FindTopNProductIds {
        @Test
        @DisplayName("랭킹 조회 성공")
        fun findFromCreatedRanking() {
            // given
            val startDate = LocalDate.now().minusDays(2)
            val endDate = LocalDate.now()
            // productId1 > productId2 > productId3 로 랭킹 세팅
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = startDate,
                    productId = ProductId(1L),
                    quantity = 10,
                    orderCount = 1
                )
            )
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = endDate,
                    productId = ProductId(2L),
                    quantity = 5,
                    orderCount = 2
                )
            )
            repository.updateRanking(
                ProductSaleRankingEntry(
                    date = endDate,
                    productId = ProductId(3L),
                    quantity = 1,
                    orderCount = 1
                )
            )
            val rankedIds = repository.renewRanking(startDate, endDate, 10)
            val limit = 2

            // when
            val result = repository.findTopNProductIds(startDate, endDate, limit)

            // then
            assertThat(result).hasSize(limit)
            assertThat(result[0]).isEqualTo(rankedIds[0])
            assertThat(result[1]).isEqualTo(rankedIds[1])
        }

        @Test
        @DisplayName("랭킹이 없는 경우 빈 리스트 반환")
        fun notExistsRanking() {
            // when
            val startDate = LocalDate.now().minusDays(2)
            val endDate = LocalDate.now()
            val result = repository.findTopNProductIds(startDate, endDate, 10)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("요청보다 랭킹된 상품이 적은 경우에 정상 응답")
        fun lessThanRequest() {
            // given
            val date = LocalDate.now()
            val productCountInRanking = 3
            repeat(productCountInRanking) {
                repository.updateRanking(
                    ProductSaleRankingEntry(
                        date = date,
                        productId = ProductMock.id(),
                        quantity = 1,
                        orderCount = 2,
                    )
                )
            }
            repository.renewRanking(date, date, productCountInRanking)
            val limit = 5

            // when
            val result = repository.findTopNProductIds(date, date, limit)

            // then
            assertThat(result).hasSize(productCountInRanking)
        }
    }
}
