package kr.hhplus.be.server.application.product

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.product.command.AggregateProductDailySalesFacadeCommand
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.domain.product.command.RecordProductDailySalesCommand
import kr.hhplus.be.server.domain.stock.StockService
import kr.hhplus.be.server.testutil.mock.OrderMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import kr.hhplus.be.server.util.TimeZone
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ProductFacadeTest {

    @InjectMockKs
    private lateinit var facade: ProductFacade

    @MockK(relaxed = true)
    private lateinit var productsService: ProductService

    @MockK(relaxed = true)
    private lateinit var stockService: StockService


    @BeforeEach
    fun setUp() {
        clearMocks(productsService, stockService)
    }

    @Test
    fun `aggregate - 상품 일일 판매량 집계 - 일자별, 상품id별로 판매 수량을 집계한다`() {
        val today = LocalDate.now(TimeZone.KSTId)
        val todayInstant = today.atStartOfDay(TimeZone.KSTId).toInstant()
        val yesterday = today.minusDays(1)
        val yesterdayInstant = yesterday.atStartOfDay(TimeZone.KSTId).toInstant()

        val sales = listOf(
            OrderMock.orderProductSnapshot(
                productId = 1L,
                quantity = 10,
                createdAt = yesterdayInstant,
            ),
            OrderMock.orderProductSnapshot(
                productId = 1L,
                quantity = 10,
                createdAt = yesterdayInstant,
            ),
            OrderMock.orderProductSnapshot(
                productId = 1L,
                quantity = 10,
                createdAt = todayInstant,
            ),
            OrderMock.orderProductSnapshot(
                productId = 2L,
                quantity = 10,
                createdAt = todayInstant,
            ),
        )
        val expects = listOf(
            RecordProductDailySalesCommand.ProductSale(
                productId = ProductId(1L),
                date = yesterday,
                quantity = 20,
            ),
            RecordProductDailySalesCommand.ProductSale(
                productId = ProductId(1L),
                date = today,
                quantity = 10,
            ),
            RecordProductDailySalesCommand.ProductSale(
                productId = ProductId(2L),
                date = today,
                quantity = 10,
            )
        )
        val command = AggregateProductDailySalesFacadeCommand(
            sales = sales,
        )

        facade.aggregate(command)

        verify {
            productsService.aggregateProductDailySales(
                withArg<RecordProductDailySalesCommand> {
                    it.sales.forEach { productSale ->
                        val expect =
                            expects.find { it.productId == productSale.productId && it.date == productSale.date }
                        assertThat(productSale.productId).isEqualTo(expect?.productId)
                        assertThat(productSale.date).isEqualTo(expect?.date)
                        assertThat(productSale.quantity).isEqualTo(expect?.quantity)
                    }
                }
            )
        }
    }

    @Test
    fun `aggregate - 상품 일일 판매량 집계 - 주문 상품이 비어 있으면 집계를 하지 않는다`() {
        val command = AggregateProductDailySalesFacadeCommand(
            sales = emptyList(),
        )

        facade.aggregate(command)

        verify(exactly = 0) {
            productsService.aggregateProductDailySales(ofType(RecordProductDailySalesCommand::class))
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
            productsService.getAllByStatusOnPaged(ProductStatus.ON_SALE, page, pageSize)
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
        every { productsService.getAllByStatusOnPaged(ProductStatus.ON_SALE, any(), any()) } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.value).isEmpty()
    }

    @Test
    fun `getAllOnSalePaged - 유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = 0
        val pageSize = 0
        coEvery {
            productsService.getAllByStatusOnPaged(
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
        val popularProducts = PopularProductsView(
            products = products,
        )
        val stocks = products.map { StockMock.view(productId = it.id) }

        every { productsService.getPopularProducts() } returns popularProducts
        every {
            stockService.getStocks(any())
        } returns stocks

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
        every { productsService.getPopularProducts() } returns PopularProductsView(emptyList<ProductView>())

        val result = facade.getPopularProducts()

        assertThat(result.value).isEmpty()
    }
}
