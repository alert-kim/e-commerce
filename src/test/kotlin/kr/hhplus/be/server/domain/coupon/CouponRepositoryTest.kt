package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import kr.hhplus.be.server.testutil.assertion.CouponAssert.Companion.assertCoupon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class CouponRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: CouponRepository

    @Test
    fun `save - 쿠폰을 저장하고 ID가 할당됨`() {
        val coupon = CouponMock.coupon(id = null)

        val saved = repository.save(coupon)

        shouldNotThrowAny {
            saved.id()
        }
        assertCoupon(saved).isEqualTo(coupon)
    }

    @Test
    fun `findById - ID로 쿠폰을 조회`() {
        val saved = repository.save(CouponMock.coupon(id = null))

        val result = repository.findById(saved.id().value)

        assertCoupon(result).isEqualTo(saved)
    }

    @Test
    fun `findById - 존재하지 않는 ID로 조회하면 null 반환`() {
        val result = repository.findById(IdMock.value())

        assertThat(result).isNull()
    }

    @Test
    fun `findAllByUserIdAndUsedAtIsNull - 사용자의 미사용 쿠폰 목록 조회`() {
        val userId = UserMock.id(1L)
        val otherUserId = UserMock.id(2L)
        val unusedCouponIds = List(2) {
            repository.save(CouponMock.coupon(id = null, userId = userId, usedAt = null))
        }.map { it.id() }
        repository.save(CouponMock.coupon(id = null, userId = otherUserId, usedAt = null))
        repository.save(CouponMock.coupon(id = null, userId = userId, usedAt = Instant.now()))

        val coupons = repository.findAllByUserIdAndUsedAtIsNull(userId)

        assertThat(coupons).hasSize(unusedCouponIds.size)
        coupons.forEach { coupon ->
            assertThat(coupon.id()).isIn(unusedCouponIds)
        }
    }
}
