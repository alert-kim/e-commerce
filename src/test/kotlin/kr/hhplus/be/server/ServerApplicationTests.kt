package kr.hhplus.be.server

import kr.hhplus.be.server.domain.balance.BalanceRecordService
import kr.hhplus.be.server.domain.balance.BalanceRepository
import kr.hhplus.be.server.domain.user.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class ServerApplicationTests {

	@MockitoBean
	lateinit var userRepository: UserRepository

	@MockitoBean
	lateinit var balanceRepository: BalanceRepository

	@MockitoBean
	lateinit var balanceRecordService: BalanceRecordService

	@Test
	fun contextLoads() {}

}
