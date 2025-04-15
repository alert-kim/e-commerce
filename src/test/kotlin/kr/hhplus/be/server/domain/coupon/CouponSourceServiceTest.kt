package kr.hhplus.be.server.domain.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

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
    fun `getAllIssuable - ACTIVE 상태의 쿠폰 소스를 조회`() {
        val couponSources = List(3) { CouponMock.source() }
        every { repository.findAllByStatus(CouponSourceStatus.ACTIVE)  } returns couponSources

        val result = service.getAllIssuable()

        assertThat(result).isEqualTo(couponSources)
        verify {
            repository.findAllByStatus(withArg {
                assertThat(it).isEqualTo(CouponSourceStatus.ACTIVE)
            })
        }
    }

    @Test
    fun `getAllIssuable - 발급 가능한 쿠폰 소스가 없는 경우 빈 리스트 반환`() {
        every { repository.findAllByStatus(CouponSourceStatus.ACTIVE)  } returns emptyList<CouponSource>()

        val result = service.getAllIssuable()

        assertThat(result).isEmpty()
    }
}
