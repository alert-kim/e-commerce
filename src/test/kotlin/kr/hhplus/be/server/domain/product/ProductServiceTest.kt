package kr.hhplus.be.server.domain.product

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.common.InvalidPageRequestArgumentException
import kr.hhplus.be.server.domain.product.command.AllocateStocksCommand
import kr.hhplus.be.server.domain.product.excpetion.NotFoundProductException
import kr.hhplus.be.server.domain.product.excpetion.OutOfStockProductException
import kr.hhplus.be.server.mock.ProductMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@ExtendWith(MockKExtension::class)
class ProductServiceTest {
    @InjectMockKs
    private lateinit var service: ProductService

    @MockK(relaxed = true)
    private lateinit var repository: ProductRepository

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
            assertThat(product.price).isEqualByComparingTo(products[index].price)
            assertThat(product.stock.quantity).isEqualTo(products[index].stock.quantity)
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
        val command = AllocateStocksCommand(
            needStocks = listOf(
                AllocateStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 10,
                ),
                AllocateStocksCommand.NeedStock(
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
        val command = AllocateStocksCommand(
            needStocks = listOf(
                AllocateStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 5,
                ),
                AllocateStocksCommand.NeedStock(
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
        val command = AllocateStocksCommand(
            needStocks = listOf(
                AllocateStocksCommand.NeedStock(
                    productId = product1Id.value,
                    quantity = 5,
                ),
                AllocateStocksCommand.NeedStock(
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
}
