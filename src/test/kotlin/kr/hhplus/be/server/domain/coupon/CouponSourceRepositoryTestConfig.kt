package kr.hhplus.be.server.domain.coupon

import kr.hhplus.be.server.infra.coupon.persistence.CouponSourceJpaRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class CouponSourceRepositoryTestConfig {

    @Bean
    fun testCouponSourceRepository(couponSourceJpaRepository: CouponSourceJpaRepository): TestCouponSourceRepository =
        TestCouponSourceRepository(couponSourceJpaRepository)
}

class TestCouponSourceRepository(
    private val couponSourceJpaRepository: CouponSourceJpaRepository
) {
    fun clear() {
        couponSourceJpaRepository.deleteAll()
    }
}
