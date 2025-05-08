package kr.hhplus.be.server.testutil

import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.coupon.Coupon
import kr.hhplus.be.server.domain.coupon.CouponSource
import kr.hhplus.be.server.domain.coupon.CouponSourceId
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductDailySaleStat
import kr.hhplus.be.server.domain.product.ProductDailySaleStatRepositoryTestConfig
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductRepositoryTestConfig
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.product.TestProductDailySaleStatRepository
import kr.hhplus.be.server.domain.product.TestProductRepository
import kr.hhplus.be.server.domain.stock.StockRepository
import kr.hhplus.be.server.domain.user.TestUserRepository
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserRepositoryTestConfig
import kr.hhplus.be.server.testutil.mock.BalanceMock
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.ProductMock
import kr.hhplus.be.server.testutil.mock.StockMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Component
@Import(UserRepositoryTestConfig::class, ProductRepositoryTestConfig::class, ProductDailySaleStatRepositoryTestConfig::class)
class DatabaseTestHelper(
    private val testUserRepository: TestUserRepository,
    private val testProductRepository: TestProductRepository,
    private val testProductDailySaleStatRepository: TestProductDailySaleStatRepository,
    private val balanceRepository: BalanceRepository,
    private val couponRepository: CouponRepository,
    private val couponSourceRepository: CouponSourceRepository,
    private val stockRepository: StockRepository,
) {
    fun savedUser() = testUserRepository.save(UserMock.user(id = null))

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = balanceRepository.save(BalanceMock.balance(id = null, userId = userId, amount = amount))

    fun savedCouponSource(
        name: String = "테스트 쿠폰",
        discountAmount: BigDecimal = BigDecimal.valueOf(1000),
        initialQuantity: Int = 20,
        quantity: Int = 10,
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE
    ): CouponSource {
        val source = CouponMock.source(
            id = null,
            name = name,
            discountAmount = discountAmount,
            initialQuantity = initialQuantity,
            quantity = quantity,
            status = status
        )
        return couponSourceRepository.save(source)
    }

    fun savedCoupon(
        userId: UserId = UserMock.id(),
        couponSourceId: CouponSourceId = CouponMock.sourceId(),
        name: String = "테스트 쿠폰",
        discountAmount: BigDecimal = 1000.toBigDecimal(),
        usedAt: Instant? = null
    ): Coupon {
        val now = Instant.now()
        val coupon = CouponMock.coupon(
            id = null,
            userId = userId,
            name = name,
            couponSourceId = couponSourceId,
            discountAmount = discountAmount,
            createdAt = now,
            usedAt = usedAt,
            updatedAt = now
        )
        return couponRepository.save(coupon)
    }

    fun savedProduct(
        status: ProductStatus = ProductStatus.ON_SALE,
        name: String = "테스트 상품",
        description: String = "테스트 상품 설명",
        price: BigDecimal = 10000.toBigDecimal(),
        stock: Int = 10
    ): Product {
        val product = ProductMock.product(
            id = null,
            name = name,
            description = description,
            price = price,
            status = status,
        )
        val saved = testProductRepository.save(product)
        stockRepository.save(
            StockMock.stock(
                id = null,
                productId = saved.id(),
                quantity = stock,
            )
        )
        return saved
    }

    fun savedProductDailySale(
        productId: ProductId,
        date: LocalDate,
        quantity: Int,
    ): ProductDailySaleStat =
        testProductDailySaleStatRepository.save(
            ProductMock.dailySale(
                id = null,
                productId = productId,
                date = date,
                quantity = quantity,
            )
        )
}
