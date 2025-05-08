package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldNotThrowAny
import kr.hhplus.be.server.domain.RepositoryTest
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.IdMock
import kr.hhplus.be.server.testutil.assertion.CouponSourceAssert.Companion.assertCouponSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CouponSourceRepositoryTest : RepositoryTest() {
    @Autowired
    lateinit var repository: CouponSourceRepository

    @Test
    fun `save - 쿠폰 소스를 저장하고 ID가 반환됨`() {
        val couponSource = CouponMock.source(id = null)

        val saved = repository.save(couponSource)

        shouldNotThrowAny {
            saved.id()
        }

        assertCouponSource(saved).isEqualTo(couponSource)
    }

    @Test
    fun `findById - ID로 쿠폰 소스를 조회`() {
        val saved = repository.save(CouponMock.source(id = null))

        val result = repository.findById(saved.id().value)

        assertCouponSource(result).isEqualTo(result)
    }

    @Test
    fun `findById - 존재하지 않는 ID로 조회하면 null 반환`() {
        val result = repository.findById(IdMock.value())

        assertThat(result).isNull()
    }

    @Test
    fun `findAllByStatus - 특정 상태의 쿠폰 소스 목록 조회`() {
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
