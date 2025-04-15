package kr.hhplus.be.server.application.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CouponFacadeTest {

    @InjectMockKs
    private lateinit var couponFacade: CouponFacade

    @MockK(relaxed = true)
    private lateinit var couponSourceService: CouponSourceService

    @Test
    fun `getAllIssuable - 발급 가능 쿠폰 조회`() {
        val couponSources = List(3) { CouponMock.source(id = CouponMock.sourceId()) }
        every { couponSourceService.getAllIssuable() } returns couponSources

        val result = couponFacade.getAllIssuable()

        assertThat(result.size).isEqualTo(couponSources.size)
        couponSources.forEachIndexed { index, couponSource ->
            assertThat(result[index].id).isEqualTo(couponSource.id)
        }
        verify {
            couponSourceService.getAllIssuable()
        }
    }
}
