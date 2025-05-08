package kr.hhplus.be.server.application.product

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.stat.PopularProductsIds
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.ProductView
import kr.hhplus.be.server.domain.product.ProductsView
import kr.hhplus.be.server.domain.product.stat.ProductSaleStatService
import kr.hhplus.be.server.domain.product.stat.command.CreateProductDailySaleStatsCommand
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import kr.hhplus.be.server.util.TimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class ProductFacadeTest {
    private val productService = mockk<ProductService>(relaxed = true)
    private val stockService = mockk<StockService>(relaxed = true)
    private val productSaleStatService = mockk<ProductSaleStatService>(relaxed = true)
    private val facade = ProductFacade(productService, productSaleStatService, stockService)

    @BeforeEach
    fun setUp() {
        clearMocks(productService, stockService, productSaleStatService)
    }

    @Test
    fun `aggregate - 상품 일일 판매량 집계 - 해당 일자로 일일 판매량을 집계한다`() {
        val command = AggregateProductDailySalesFacadeCommand(
            date = LocalDate.now(TimeZone.KSTId),
        )

        facade.aggregate(command)

        verify(exactly = 1) {
            productSaleStatService.createDailyStats(CreateProductDailySaleStatsCommand(command.date))
        }
    }

    @Test
    fun `getAllOnSalePaged - 판매 중인 상품 목록을 페이징하여 조회`() {
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
    fun `getAllOnSalePaged - 판매 중인 상품이 없는 경우 빈 페이지 반환`() {
        val page = 0
        val pageSize = 10
        val productPage = PageImpl(emptyList<ProductView>())
        every { productService.getAllByStatusOnPaged(ProductStatus.ON_SALE, any(), any()) } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.value).isEmpty()
    }

    @Test
    fun `getAllOnSalePaged - 유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = 0
        val pageSize = 0
        coEvery {
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

    @Test
    fun `getPopularProducts - 인기 상품 목록을 조회`() {
        val products = List(2) {
            ProductMock.view()
        }
        val popularProductIds = products.map { it.id }
        val stocks = products.map { StockMock.view(productId = it.id) }
        every { productSaleStatService.getPopularProductIds() } returns PopularProductsIds(popularProductIds)
        every { productService.getAllByIds(popularProductIds.map { it.value }) } returns ProductsView(products)
        every { stockService.getStocks(any()) } returns stocks

        val result = facade.getPopularProducts()

        assertThat(result.value).hasSize(products.size)
        result.value.forEachIndexed { index, productWithStock ->
            val product = products[index]
            val stock = stocks[index]

            assertThat(productWithStock.product.id.value).isEqualTo(product.id.value)
            assertThat(productWithStock.stockQuantity).isEqualTo(stock.quantity)
        }
    }

    @Test
    fun `getPopularProducts - 인기 상품이 없는 경우 빈 페이지 반환`() {
        every { productSaleStatService.getPopularProductIds() } returns PopularProductsIds(emptyList())

        val result = facade.getPopularProducts()

        assertThat(result.value).isEmpty()
    }
}
