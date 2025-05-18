package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.testutil.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CouponSourceServiceTest {
    
    private val repository = mockk<CouponSourceRepository>(relaxed = true)
    private lateinit var service: CouponSourceService

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
        service = CouponSourceService(repository)
    }

    @Nested
    @DisplayName("쿠폰 발급")
    inner class Issue {
        @Test
        @DisplayName("발급 성공")
        fun success() {
            val couponSourceId = CouponSourceId(1L)
            val couponSource = mockk<CouponSource>()
            val issuedCoupon = CouponMock.issuedCoupon()
            every { repository.findById(couponSourceId.value) } returns couponSource
            every { couponSource.issue() } returns issuedCoupon

            val result = service.issue(IssueCouponCommand(couponSourceId.value))

            assertThat(result).isEqualTo(issuedCoupon)
            verify {
                repository.findById(couponSourceId.value)
            }
        }

        @Test
        @DisplayName("쿠폰 소스 없음")
        fun notFound() {
            val couponSourceId = CouponSourceId(1L)
            every { repository.findById(couponSourceId.value) } returns null

            shouldThrow<NotFoundCouponSourceException> {
                service.issue(IssueCouponCommand(couponSourceId.value))
            }

            verify(exactly = 0) {
                repository.save(any())
            }
        }
    }

    @Nested
    @DisplayName("발급 가능 쿠폰 조회")
    inner class GetAllIssuable {
        @Test
        @DisplayName("ACTIVE 상태 쿠폰 소스 조회")
        fun active() {
            val couponSources = List(3) { CouponMock.source() }
            every { repository.findAllByStatus(CouponSourceStatus.ACTIVE) } returns couponSources

            val result = service.getAllIssuable()

            result.forEachIndexed { index, source ->
                assertThat(source.id).isEqualTo(couponSources[index].id())
            }
            verify {
                repository.findAllByStatus(withArg {
                    assertThat(it).isEqualTo(CouponSourceStatus.ACTIVE)
                })
            }
        }

        @Test
        @DisplayName("발급 가능 쿠폰 없음")
        fun empty() {
            every { repository.findAllByStatus(CouponSourceStatus.ACTIVE) } returns emptyList<CouponSource>()

            val result = service.getAllIssuable()

            assertThat(result).isEmpty()
        }
    }
}
