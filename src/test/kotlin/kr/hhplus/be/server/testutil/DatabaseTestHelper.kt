package kr.hhplus.be.server.testutil

import kr.hhplus.be.server.common.util.TimeZone
import kr.hhplus.be.server.domain.balance.repository.BalanceRepository
import kr.hhplus.be.server.domain.coupon.*
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderId
import kr.hhplus.be.server.domain.order.OrderProduct
import kr.hhplus.be.server.domain.order.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.payment.PaymentStatus
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository
import kr.hhplus.be.server.domain.product.*
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingEntry
import kr.hhplus.be.server.domain.product.ranking.ProductSaleRankingRepositoryTestConfig
import kr.hhplus.be.server.domain.product.ranking.TestSaleRankingRedisRepository
import kr.hhplus.be.server.domain.product.ranking.repository.ProductSaleRankingRepository
import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStat
import kr.hhplus.be.server.domain.product.stat.ProductDailySaleStatRepositoryTestConfig
import kr.hhplus.be.server.domain.product.stat.TestProductDailySaleStatRepository
import kr.hhplus.be.server.domain.stock.Stock
import kr.hhplus.be.server.domain.stock.StockRepository
import kr.hhplus.be.server.domain.user.TestUserRepository
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.domain.user.UserRepositoryTestConfig
import kr.hhplus.be.server.testutil.mock.*
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Component
@Import(
    UserRepositoryTestConfig::class,
    ProductRepositoryTestConfig::class,
    ProductDailySaleStatRepositoryTestConfig::class,
    ProductSaleRankingRepositoryTestConfig::class,
    CouponSourceRepositoryTestConfig::class,
)
class DatabaseTestHelper(
    private val testCouponSourceRepository: TestCouponSourceRepository,
    private val testUserRepository: TestUserRepository,
    private val testProductRepository: TestProductRepository,
    private val testProductDailySaleStatRepository: TestProductDailySaleStatRepository,
    private val testProductSaleRankingRepository: TestSaleRankingRedisRepository,
    private val balanceRepository: BalanceRepository,
    private val couponRepository: CouponRepository,
    private val couponSourceRepository: CouponSourceRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val productRepository: ProductRepository,
    private val productSaleRankingRepository: ProductSaleRankingRepository,
    private val stockRepository: StockRepository,
) {
    // user
    fun savedUser() = testUserRepository.save(UserMock.user(id = null))

    // balance
    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = balanceRepository.save(BalanceMock.balance(id = null, userId = userId, amount = amount))

    fun findBalance(
        userId: UserId,
    ) = balanceRepository.findByUserId(userId)

    // coupon
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

    fun clearCouponSource() {
        testCouponSourceRepository.clear()
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

    fun findCouponSource(
        id: CouponSourceId,
    ): CouponSource? = couponSourceRepository.findById(id.value)

    fun findCoupon(id: CouponId) = couponRepository.findById(id.value)

    // order
    fun savedOrder(
        userId: UserId = UserMock.id(),
        status: OrderStatus = OrderStatus.READY,
        couponId: CouponId? = null,
        originalAmount: BigDecimal = BigDecimal.valueOf(2000),
        discountAmount: BigDecimal = BigDecimal.ZERO,
        totalAmount: BigDecimal = BigDecimal.valueOf(2000),
        products: List<OrderProduct> = emptyList(),
    ): Order =
        orderRepository.save(
            OrderMock.order(
                id = null,
                userId = UserMock.id(),
                status = status,
                couponId = couponId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                totalAmount = totalAmount,
                products = products,
            )
        )

    // payment
    fun savedPayment(
        userId: UserId = UserMock.id(),
        status: PaymentStatus = PaymentStatus.COMPLETED,
        orderId: OrderId = OrderMock.id(),
        amount: BigDecimal = 2000.toBigDecimal(),
        canceledAt: Instant? = null,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ) {
        val payment = PaymentMock.payment(
            id = null,
            userId = userId,
            status = status,
            orderId = orderId,
            amount = amount,
            canceledAt = canceledAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
        paymentRepository.save(payment)
    }

    // product
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
        savedStock(
            productId = saved.id(),
            quantity = stock,
        )
        return saved
    }

    fun clearProducts() {
        testProductRepository.deleteAll()
    }

    fun findProduct(
        id: ProductId,
    ) = productRepository.findById(id.value)

    // product-ranking
    fun updateProductSaleRanking(
        productId: ProductId = ProductMock.id(),
        date: LocalDate = LocalDate.now(TimeZone.KSTId),
        rankingStartDate: LocalDate = LocalDate.now(TimeZone.KSTId).minusDays(2),
        rankingEndDate: LocalDate = LocalDate.now(TimeZone.KSTId),
        quantity: Int = 10,
    ) {
        productSaleRankingRepository.updateRanking(
            ProductSaleRankingEntry(
                productId = productId,
                date = date,
                quantity = quantity,
                orderCount = 1,
            )
        )
        productSaleRankingRepository.renewRanking(
            startDate = rankingStartDate,
            endDate = rankingEndDate,
            limit = 10
        )
    }

    fun deleteAllProductSaleRanking() {
        testProductSaleRankingRepository.deleteAll()
    }

    // stock
    fun savedStock(
        productId: ProductId = ProductMock.id(),
        quantity: Int = 10,
    ): Stock = stockRepository.save(
        StockMock.stock(
            id = null,
            productId = productId,
            quantity = quantity,
        )
    )

    fun findStock(
        productId: ProductId,
    ) = stockRepository.findByProductId(productId)

}
