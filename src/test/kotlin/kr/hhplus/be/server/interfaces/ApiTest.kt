package kr.hhplus.be.server.interfaces

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.server.domain.coupon.CouponSourceStatus
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductId
import kr.hhplus.be.server.domain.product.ProductStatus
import kr.hhplus.be.server.domain.user.UserId
import kr.hhplus.be.server.mock.BalanceMock
import kr.hhplus.be.server.mock.ProductMock
import kr.hhplus.be.server.util.DatabaseTestHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class ApiTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var databaseTestHelper: DatabaseTestHelper

    fun savedUser() = databaseTestHelper.savedUser()

    fun savedBalance(
        userId: UserId,
        amount: BigDecimal = BalanceMock.amount().value,
    ) = databaseTestHelper.savedBalance(userId, amount)

    fun savedCoupon(
        userId: UserId = savedUser().id(),
        usedAt: Instant? = null,
        discountAmount: BigDecimal = 1000.toBigDecimal(),
    ) = databaseTestHelper.savedCoupon(
        userId = userId,
        usedAt = usedAt,
        discountAmount = discountAmount,
    )

    fun savedCouponSource(
        quantity: Int = 10,
        status: CouponSourceStatus = CouponSourceStatus.ACTIVE
    ) = databaseTestHelper.savedCouponSource(
        quantity = quantity,
        status = status
    )

    fun savedProduct(
        status: ProductStatus = ProductStatus.ON_SALE,
        stock: Int = 100,
        price: BigDecimal = BigDecimal.valueOf(10_000),
    ): Product =
        databaseTestHelper.savedProduct(
            status = status,
            stock = stock,
            price = price,
        )

    fun savedProductDailySale(
        productId: ProductId = ProductMock.id(),
        date: LocalDate = LocalDate.now(),
        quantity: Int = 100,
    ) =
        databaseTestHelper.savedProductDailySale(
            productId = productId,
            date = date,
            quantity = quantity
        )

}
