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
import kr.hhplus.be.server.mock.OrderMock
import kr.hhplus.be.server.mock.ProductMock
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
    private lateinit var service: ProductService

    @BeforeEach
    fun setUp() {
        clearMocks(service)
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
            service.aggregateProductDailySales(
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
            service.aggregateProductDailySales(ofType(RecordProductDailySalesCommand::class))
        }
    }

    @Test
    fun `getAllOnSalePaged - 판매 중인 상품 목록을 페이징해서 조회 모델로 반환`() {
        val page = 0
        val pageSize = 10
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val products = List(pageSize) {
            ProductMock.product()
        }
        val totalCount = products.size * 2L
        val productPage = PageImpl(products, pageable, totalCount)
        every {
            service.getAllByStatusOnPaged(
                status = ProductStatus.ON_SALE,
                page = page,
                pageSize = pageSize
            )
        } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(totalCount)
        assertThat(result.content).hasSize(products.size)
        result.content.forEachIndexed { index, productQueryModel ->
            assertThat(productQueryModel.id).isEqualTo(products[index].id)
            assertThat(productQueryModel.status).isEqualTo(products[index].status)
            assertThat(productQueryModel.name).isEqualTo(products[index].name)
            assertThat(productQueryModel.description).isEqualTo(products[index].description)
            assertThat(productQueryModel.price).isEqualByComparingTo(products[index].price)
            assertThat(productQueryModel.stock).isEqualByComparingTo(products[index].stock.quantity)
            assertThat(productQueryModel.createdAt).isEqualTo(products[index].createdAt)
        }
        verify {
            service.getAllByStatusOnPaged(status = ProductStatus.ON_SALE, page = page, pageSize = pageSize)
        }
    }

    @Test
    fun `getAllOnSalePaged - 판매 중인 상품이 없는 경우 빈 페이지 반환`() {
        val page = 0
        val pageSize = 10
        val productPage = PageImpl(emptyList<Product>())
        every { service.getAllByStatusOnPaged(ProductStatus.ON_SALE, any(), any()) } returns productPage

        val result = facade.getAllOnSalePaged(page, pageSize)

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `getAllOnSalePaged - 유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생`() {
        val page = 0
        val pageSize = 0
        coEvery { service.getAllByStatusOnPaged(any(), any(), any()) } throws InvalidPageRequestArgumentException(
            0,
            0,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        shouldThrow<InvalidPageRequestArgumentException> {
            facade.getAllOnSalePaged(page, pageSize)
        }
    }

    @Test
    fun `getPopularProducts - 인기 상품 반환`() {
        val products = List(5) {
            ProductMock.product()
        }
        every {
            service.getPopularProducts()
        } returns PopularProducts(products)

        val result = facade.getPopularProducts().products

        assertThat(result).hasSize(products.size)
        result.forEachIndexed { index, productQueryModel ->
            assertThat(productQueryModel.id).isEqualTo(products[index].id)
            assertThat(productQueryModel.status).isEqualTo(products[index].status)
            assertThat(productQueryModel.name).isEqualTo(products[index].name)
            assertThat(productQueryModel.description).isEqualTo(products[index].description)
            assertThat(productQueryModel.price).isEqualByComparingTo(products[index].price)
            assertThat(productQueryModel.stock).isEqualByComparingTo(products[index].stock.quantity)
            assertThat(productQueryModel.createdAt).isEqualTo(products[index].createdAt)
        }
        verify {
            service.getPopularProducts()
        }
    }

    @Test
    fun `getPopularProducts - 인기 상품이 없는 경우 빈 페이지 반환`() {
        every { service.getPopularProducts() } returns PopularProducts(emptyList())

        val result = facade.getPopularProducts()

        assertThat(result.products).isEmpty()
    }
}
