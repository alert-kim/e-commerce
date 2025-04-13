package kr.hhplus.be.server

import kr.hhplus.be.server.domain.balance.BalanceRecordService
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.coupon.CouponRepository
import kr.hhplus.be.server.domain.order.OrderRepository
import kr.hhplus.be.server.domain.payment.PaymentRepository
import kr.hhplus.be.server.domain.product.ProductRepository
import kr.hhplus.be.server.domain.user.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class ServerApplicationTests {

	@MockitoBean
	lateinit var couponRepository: CouponRepository

	@MockitoBean
	lateinit var balanceRepository: BalanceRepository

	@MockitoBean
	lateinit var balanceRecordService: BalanceRecordService

	@MockitoBean
	lateinit var orderRepository: OrderRepository

	@MockitoBean
	lateinit var paymentRepository: PaymentRepository

	@MockitoBean
	lateinit var productRepository: ProductRepository

	@MockitoBean
	lateinit var userRepository: UserRepository

	@Test
	fun contextLoads() {}

}
