package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.mock.UserMock
import kr.hhplus.be.server.testutil.assertion.CouponAssert.Companion.assertCoupon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class CouponRepositoryTest @Autowired constructor(
    private val repository: CouponRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("저장")
    inner class Save {
        @Test
        @DisplayName("성공 시 ID 할당")
        fun success() {
            val coupon = CouponMock.coupon(id = null)

            val saved = repository.save(coupon)

            shouldNotThrowAny {
                saved.id()
            }
            assertCoupon(saved).isEqualTo(coupon)
        }
    }

    @Nested
    @DisplayName("조회")
    inner class Find {
        @Test
        @DisplayName("ID로 조회")
        fun findById() {
            val saved = repository.save(CouponMock.coupon(id = null))

            val result = repository.findById(saved.id().value)

            assertCoupon(result).isEqualTo(saved)
        }

        @Test
        @DisplayName("존재하지 않는 ID는 null 반환")
        fun notFound() {
            val result = repository.findById(IdMock.value())

            assertThat(result).isNull()
        }

        @Test
        @DisplayName("사용자별 미사용 쿠폰 조회")
        fun findUnusedByUser() {
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
}
