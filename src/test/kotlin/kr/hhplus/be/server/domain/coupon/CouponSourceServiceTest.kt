package kr.hhplus.be.server.domain.coupon

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponSourceException
import kr.hhplus.be.server.domain.coupon.repository.CouponSourceRepository
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CouponSourceServiceTest {
    @InjectMockKs
    private lateinit var service: CouponSourceService

    @MockK(relaxed = true)
    private lateinit var repository: CouponSourceRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
    }

    @Test
    fun `issue - 쿠폰 발급`() {
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
    fun `issue - 찾을 수 없는 쿠폰 소스`() {
        val couponSourceId = CouponSourceId(1L)
        every { repository.findById(couponSourceId.value) } returns null

        shouldThrow<NotFoundCouponSourceException> {
            service.issue(IssueCouponCommand(couponSourceId.value))
        }

        verify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `getAllIssuable - ACTIVE 상태의 쿠폰 소스를 조회`() {
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
    fun `getAllIssuable - 발급 가능한 쿠폰 소스가 없는 경우 빈 리스트 반환`() {
        every { repository.findAllByStatus(CouponSourceStatus.ACTIVE) } returns emptyList<CouponSource>()

        val result = service.getAllIssuable()

        assertThat(result).isEmpty()
    }
}
