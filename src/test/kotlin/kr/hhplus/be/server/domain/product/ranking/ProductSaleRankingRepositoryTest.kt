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
class ProductSaleRankingRepositoryTest {
    @Autowired
    lateinit var repository: ProductSaleRankingRepository

    @Autowired
    lateinit var testRepository: TestSaleRankingRedisRepository

    @BeforeEach
    fun setup() {
        testRepository.deleteAll()
    }

    @Nested
    @DisplayName("랭킹 점수 업데이트")
    inner class UpdateRankingTest {

        @Test
        @DisplayName("상품 판매 랭킹 정보가 저장된다(판매수량 + 주문수)")
        fun updateRanking() {
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
        @DisplayName("동일한 날짜, 동일한 상품 ID에 대해 점수가 누적된다")
        fun scoreIsAccumulated() {
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
    @DisplayName("랭킹 집계")
    inner class RenewRankingTest {

        @Test
        @DisplayName("여러 날짜의 점수 합쳐서 통합 랭킹을 생성한다")
        fun createRanking() {
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
        @DisplayName("판매 수량이 같으면 주문 수량이 많은 것이 랭킹이 더 높다")
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
        @DisplayName("기존 랭킹이 있는 경우, 새로 추가된 점수를 합산한다")
        fun renewRanking() {
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
        @DisplayName("랭킹 항목이 없으면 빈 리스트를 반환한다")
        fun emptyRanking() {
            // given
            val startDate = LocalDate.now().minusDays(3)
            val endDate = LocalDate.now()

            // when
            val result = repository.renewRanking(startDate, endDate, 10)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("제한된 개수만큼만 상위 항목을 반환한다")
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
        @DisplayName("유효하지 않은 날짜 범위가 주어지면 예외를 발생시킨다")
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
    @DisplayName("상위 N개 상품 ID 조회")
    inner class FindTopNProductIdsTest {

        @Test
        @DisplayName("생성된 통합 랭킹에서 상위 N개 상품 ID를 조회한다")
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
        @DisplayName("통합 랭킹이 없으면 빈 리스트를 반환한다")
        fun notExistsRanking() {
            // when
            val startDate = LocalDate.now().minusDays(2)
            val endDate = LocalDate.now()
            val result = repository.findTopNProductIds(startDate, endDate, 10)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        @DisplayName("요청한 개수보다 적은 결과가 있으면 가능한 만큼 반환한다")
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
