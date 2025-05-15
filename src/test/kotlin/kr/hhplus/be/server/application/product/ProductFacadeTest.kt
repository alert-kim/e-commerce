package kr.hhplus.be.server.application.product

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.product.ProductsView
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingService
import kr.hhplus.be.server.domain.product.stat.PopularProductsIds
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class ProductFacadeTest {
    private val productService = mockk<ProductService>(relaxed = true)
    private val stockService = mockk<StockService>(relaxed = true)
    private val productSaleStatService = mockk<ProductSaleStatService>(relaxed = true)
    private val productSaleRankingService = mockk<ProductSaleRankingService>(relaxed = true)
    private val facade = ProductFacade(productService, productSaleStatService, productSaleRankingService, stockService)

    @BeforeEach
    fun setUp() {
        clearMocks(productService, stockService, productSaleStatService, productSaleRankingService)
    }

    @Nested
    @DisplayName("상품 일일 판매량 집계")
    inner class AggregateProductDailySales {
        
        @Test
        @DisplayName("해당 일자로 일일 판매량을 집계한다")
        fun aggregate() {
            val command = AggregateProductDailySalesFacadeCommand(
                date = LocalDate.now(TimeZone.KSTId),
            )

            facade.aggregate(command)

            verify(exactly = 1) {
                productSaleStatService.createDailyStats(CreateProductDailySaleStatsCommand(command.date))
            }
        }
    }

    @Nested
    @DisplayName("인기 상품 갱신")
    inner class RenewPopularProduct {

        @Test
        @DisplayName("해당 일자로 인기 상품을 갱신한다")
        fun aggregate() {
            val command = AggregateProductDailySalesFacadeCommand(
                date = LocalDate.now(TimeZone.KSTId),
            )

            facade.aggregate(command)

            verify(exactly = 1) {
                productSaleStatService.createDailyStats(CreateProductDailySaleStatsCommand(command.date))
            }
        }
    }

    @Nested
    @DisplayName("판매 중인 상품 목록 페이징 조회")
    inner class GetAllOnSalePaged {
        
        @Test
        @DisplayName("판매 중인 상품 목록을 페이징하여 조회한다")
        fun success() {
            val page = 0
            val pageSize = 10
            val products = List(2) {
                ProductMock.view()
            }
            val stocks = products.map { StockMock.view(productId = it.id) }
            val productPage = PageImpl(
                products,
                PageRequest.of(page, pageSize),
                products.size.toLong()
            )
            every {
                productService.getAllByStatusOnPaged(ProductStatus.ON_SALE, page, pageSize)
            } returns productPage
            every {
                stockService.getStocks(any())
            } returns stocks

            val result = facade.getAllOnSalePaged(page, pageSize)

            assertThat(result.value.totalElements).isEqualTo(products.size.toLong())
            assertThat(result.value.number).isEqualTo(productPage.number)
            assertThat(result.value.size).isEqualTo(productPage.size)
            result.value.content.forEachIndexed { index, productWithStock ->
                val product = products[index]
                val stock = stocks[index]

                assertThat(productWithStock.product.id.value).isEqualTo(product.id.value)
                assertThat(productWithStock.stockQuantity).isEqualTo(stock.quantity)
            }
        }

        @Test
        @DisplayName("판매 중인 상품이 없는 경우 빈 페이지를 반환한다")
        fun emptyResult() {
            val page = 0
            val pageSize = 10
            val productPage = PageImpl(emptyList<ProductView>())
            every { productService.getAllByStatusOnPaged(ProductStatus.ON_SALE, any(), any()) } returns productPage

            val result = facade.getAllOnSalePaged(page, pageSize)

            assertThat(result.value).isEmpty()
        }

        @Test
        @DisplayName("유효하지 않은 페이징 요청 값인 경우 예외가 발생한다")
        fun invalidPageRequest() {
            val page = 0
            val pageSize = 0
            every {
                productService.getAllByStatusOnPaged(
                    any(),
                    any(),
                    any()
                )
            } throws InvalidPageRequestArgumentException(
                0,
                0,
                Sort.by(Sort.Direction.DESC, "createdAt")
            )

            shouldThrow<InvalidPageRequestArgumentException> {
                facade.getAllOnSalePaged(page, pageSize)
            }
        }
    }

    @Nested
    @DisplayName("인기 상품 목록 조회")
    inner class GetPopularProducts {
        
        @Test
        @DisplayName("인기 상품 목록을 조회한다")
        fun success() {
            val products = List(2) {
                ProductMock.view()
            }
            val popularProductIds = products.map { it.id }
            val stocks = products.map { StockMock.view(productId = it.id) }
            val today = LocalDate.now(TimeZone.KSTId)
            every { productSaleRankingService.getPopularProductIds() } returns PopularProductsIds(popularProductIds)
            every { productService.getAllByIds(any()) } returns ProductsView(products)
            every { stockService.getStocks(any()) } returns stocks

            val result = facade.getPopularProducts()

            assertThat(result.value).hasSize(products.size)
            result.value.forEachIndexed { index, productWithStock ->
                val product = products[index]
                val stock = stocks[index]

                assertThat(productWithStock.product.id.value).isEqualTo(product.id.value)
                assertThat(productWithStock.stockQuantity).isEqualTo(stock.quantity)
            }
            verify {
                productSaleRankingService.getPopularProductIds()
                productService.getAllByIds(popularProductIds.map { it.value })
                stockService.getStocks(popularProductIds)
            }
        }

        @Test
        @DisplayName("인기 상품이 없는 경우 빈 목록을 반환한다")
        fun emptyResult() {
            every { productSaleRankingService.getPopularProductIds() } returns PopularProductsIds(emptyList())

            val result = facade.getPopularProducts()

            assertThat(result.value).isEmpty()
        }
    }
}
