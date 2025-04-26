package kr.hhplus.be.server.domain.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.command.AllocateProductStocksCommand
import kr.hhplus.be.server.domain.product.command.RecordProductDailySalesCommand
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.product.excpetion.OutOfStockProductException
import kr.hhplus.be.server.domain.product.repository.ProductDailySaleRepository
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
class ProductServiceTest {
    @InjectMockKs
    private lateinit var service: ProductService

    @MockK(relaxed = true)
    private lateinit var repository: ProductRepository

    @MockK(relaxed = true)
    private lateinit var saleRepository: ProductDailySaleRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
    }

    @Test
    fun `getAllByStatusPaged - 해당 상태의 상품 목록을 페이징해서 조회 모델로 반환`() {
        val status = ProductStatus.entries.random()
        val page = 0
        val pageSize = 10
        val pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val products = List(pageSize) {
            ProductMock.product()
        }
        val totalCount = products.size * 2L
        val productPage = PageImpl(products, pageable, totalCount)
        every { repository.findAllByStatus(status, pageable) } returns productPage

        val result = service.getAllByStatusOnPaged(status, page, pageSize)

        assertThat(result.pageable).isEqualTo(pageable)
        assertThat(result.totalElements).isEqualTo(totalCount)
        assertThat(result.content).hasSize(products.size)
        result.content.forEachIndexed { index, product ->
            assertThat(product.id).isEqualTo(products[index].id)
            assertThat(product.status).isEqualTo(products[index].status)
            assertThat(product.name).isEqualTo(products[index].name)
            assertThat(product.description).isEqualTo(products[index].description)
            assertThat(product.price.value).isEqualByComparingTo(products[index].price)
            assertThat(product.stock).isEqualTo(products[index].stock.quantity)
            assertThat(product.createdAt).isEqualTo(products[index].createdAt)
        }
        verify {
            repository.findAllByStatus(status, pageable)
        }
    }

    @Test
    fun `getAllByStatusPaged - 해당 상품이 없는 경우 빈 페이지 반환`() {
        val status = ProductStatus.ON_SALE
        val page = 0
        val pageSize = 10
        val productPage = PageImpl(emptyList<Product>())
        every { repository.findAllByStatus(status, any()) } returns productPage

        val result = service.getAllByStatusOnPaged(status, page, pageSize)

        assertThat(result.content).isEmpty()
    }

    @Test
    fun `getAllOnSalePaged - 유효하지 않은 페이징 요청 값인 경우 InvalidPageRequestArgumentException 발생`() {
        val status = ProductStatus.ON_SALE
        val page = 0
        val pageSize = 0

        shouldThrow<InvalidPageRequestArgumentException> {
            service.getAllByStatusOnPaged(status, page, pageSize)
        }

        verify(exactly = 0) {
            repository.findAllByStatus(any(), any())
        }
    }

    @Test
    fun `allocateStocks - 상품 재고 할당 성공`() {
        val product1Id = ProductMock.id()
        val product1 = ProductMock.product(id = product1Id, stock = ProductMock.stock(quantity = 20))
        val product2Id = ProductMock.id()
        val product2 = ProductMock.product(id = product2Id, stock = ProductMock.stock(quantity = 15))
        val command = AllocateProductStocksCommand(
            needStocks = listOf(
                AllocateProductStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 10,
                ),
                AllocateProductStocksCommand.NeedStock(
                    productId = product2Id.value,
                    quantity = 15,
                ),
            ),
        )
        every { repository.findAllByIds(listOf(product1Id.value, product2Id.value)) } returns listOf(product1, product2)

        val result = service.allocateStocks(command)

        assertAll(
            { assertThat(result.stocks).hasSize(2) },
            { assertThat(result.stocks[0].productId).isEqualTo(product1Id) },
            { assertThat(result.stocks[0].quantity).isEqualTo(10) },
            { assertThat(result.stocks[1].productId).isEqualTo(product2Id) },
            { assertThat(result.stocks[1].quantity).isEqualTo(15) },
        )
        verify {
            repository.findAllByIds(listOf(product1Id.value, product2Id.value))
            repository.save(withArg { assertThat(it.id).isEqualTo(product1Id) })
            repository.save(withArg { assertThat(it.id).isEqualTo(product2Id) })
        }
    }

    @Test
    fun `allocateStocks - 상품 재고 할당 실패 - 재고 부족, OutOfStockProductException 발생`() {
        val product1Id = ProductMock.id()
        val product1 = ProductMock.product(id = product1Id, stock = ProductMock.stock(quantity = 5))
        val product2Id = ProductMock.id()
        val product2 = ProductMock.product(id = product2Id, stock = ProductMock.stock(quantity = 5))
        val command = AllocateProductStocksCommand(
            needStocks = listOf(
                AllocateProductStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 5,
                ),
                AllocateProductStocksCommand.NeedStock(
                    productId = product2Id.value,
                    quantity = 15,
                ),
            ),
        )
        every { repository.findAllByIds(listOf(product1Id.value, product2Id.value)) } returns listOf(product1, product2)

        shouldThrow<OutOfStockProductException> {
            service.allocateStocks(command)
        }
    }

    @Test
    fun `allocateStocks - 상품 재고 할당 실패 - 상품 없음, NotFoundProductException 발생`() {
        val product1Id = ProductMock.id()
        val product1 = ProductMock.product(id = product1Id, stock = ProductMock.stock(quantity = 5))
        val product2Id = ProductMock.id()
        val command = AllocateProductStocksCommand(
            needStocks = listOf(
                AllocateProductStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 5,
                ),
                AllocateProductStocksCommand.NeedStock(
                    productId = product2Id.value,
                    quantity = 15,
                ),
            ),
        )
        every { repository.findAllByIds(listOf(product1Id.value, product2Id.value)) } returns listOf(product1)

        shouldThrow<NotFoundProductException> {
            service.allocateStocks(command)
        }
    }

    @Test
    fun `aggregateProductDailySales - 상품 일일 판매량 집계 - 해당 상품의 집계 데이터가 이미 있는 경우`() {
        val newSaleQuantity = 10
        val sale = ProductMock.dailySale()
        val originalQuantity = sale.quantity

        val command = RecordProductDailySalesCommand(
            sales = listOf(
                RecordProductDailySalesCommand.ProductSale(
                    productId = sale.productId,
                    date = sale.date,
                    quantity = newSaleQuantity,
                ),
            ),
        )
        every {
            saleRepository.findByProductIdAndDate(
                productId = sale.productId,
                date = sale.date,
            )
        } returns sale

        service.aggregateProductDailySales(command)

        verify {
            saleRepository.update(withArg<ProductDailySale> {
                assertThat(it.productId).isEqualTo(sale.productId)
                assertThat(it.date).isEqualTo(sale.date)
                assertThat(it.quantity).isEqualTo(originalQuantity + newSaleQuantity)
            })
        }
    }

    @Test
    fun `aggregateProductDailySales - 상품 일일 판매량 집계 - 해당 상품의 집계 데이터가 없는 경우`() {
        val date = LocalDate.now()
        val productId = ProductMock.id()
        val newSaleQuantity = 3

        val command = RecordProductDailySalesCommand(
            sales = listOf(
                RecordProductDailySalesCommand.ProductSale(
                    productId = productId,
                    date = date,
                    quantity = newSaleQuantity,
                ),
            ),
        )
        every {
            saleRepository.findByProductIdAndDate(
                productId = productId,
                date = date,
            )
        } returns null

        service.aggregateProductDailySales(command)

        verify {
            saleRepository.save(withArg<ProductDailySale> {
                assertThat(it.productId).isEqualTo(productId)
                assertThat(it.date).isEqualTo(date)
                assertThat(it.quantity).isEqualTo(newSaleQuantity)
            })
        }
    }


    @Test
    fun `getAllByIds - 주어진 ID 목록에 해당하는 상품들을 반환`() {
        val ids = listOf(1L, 2L, 3L)
        val products = ids.map { ProductMock.product(id = ProductId(it)) }
        every { repository.findAllByIds(ids) } returns products

        val result = service.getAllByIds(ids)

        assertThat(result.value).hasSize(ids.size)
        assertThat(result.value.map { it.id }).containsExactlyInAnyOrderElementsOf(products.map { it.id })
    }

    @Test
    fun `getAllByIds - 주어진 ID 목록에 해당하는 상품이 없으면 빈 리스트 반환`() {
        val ids = listOf(1L, 2L, 3L)
        every { repository.findAllByIds(ids) } returns emptyList()

        val result = service.getAllByIds(ids)

        assertThat(result.value).isEmpty()
    }

    @Test
    fun `getPopularProducts - 인기 상품 조회`() {
        val products = List(Arb.int(1..PopularProducts.MAX_SIZE).next()) {
            ProductMock.product()
        }
        val sales = products.map { ProductMock.dailySale(productId = it.requireId()) }
        val productsIds = products.map { it.requireId().value }
        every { saleRepository.findTopNProductsByQuantity(
            startDate = PopularProducts.getStartDay(),
            endDate = PopularProducts.getEndDay(),
            limit = PopularProducts.MAX_SIZE,
        ) } returns sales
        every { repository.findAllByIds(productsIds) } returns products

        val result = service.getPopularProducts()

        assertThat(result.products).hasSize(products.size)
        result.products.forEachIndexed { index, product ->
            assertThat(product.id).isEqualTo(products[index].id)
        }
    }
}
