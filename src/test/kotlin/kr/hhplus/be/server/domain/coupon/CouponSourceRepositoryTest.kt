package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.assertion.CouponSourceAssert.Companion.assertCouponSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CouponSourceRepositoryTest @Autowired constructor(
    private val repository: CouponSourceRepository
) : RepositoryTest() {

    @Nested
    @DisplayName("저장")
    inner class Save {
        @Test
        @DisplayName("성공 시 ID 반환")
        fun success() {
            val couponSource = CouponMock.source(id = null)

            val saved = repository.save(couponSource)

            shouldNotThrowAny {
                saved.id()
            }

            assertCouponSource(saved).isEqualTo(couponSource)
        }
    }

    @Nested
    @DisplayName("조회")
    inner class Find {
        @Test
        @DisplayName("ID로 조회")
        fun findById() {
            val saved = repository.save(CouponMock.source(id = null))

            val result = repository.findById(saved.id().value)

            assertCouponSource(result).isEqualTo(result)
        }

        @Test
        @DisplayName("존재하지 않는 ID는 null 반환")
        fun findByIdNotFound() {
            val result = repository.findById(IdMock.value())

            assertThat(result).isNull()
        }

        @Test
        @DisplayName("상태별 목록 조회")
        fun findAllByStatus() {
            val status = CouponSourceStatus.ACTIVE
            val activeCouponIds = List(3) {
                repository.save(CouponMock.source(id = null, status = status))
            }.map { it.id() }
            repository.save(CouponMock.source(id = null, status = CouponSourceStatus.OUT_OF_STOCK))

            val result = repository.findAllByStatus(status)

            val resultIds = result.map { it.id() }.toSet()
            assertThat(result.size).isGreaterThanOrEqualTo(activeCouponIds.size)
            assertThat(resultIds).containsAll(activeCouponIds)
        }
    }
}
