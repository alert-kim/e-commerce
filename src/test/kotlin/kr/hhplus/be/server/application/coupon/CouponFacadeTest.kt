package kr.hhplus.be.server.application.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.mock.UserMock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CouponFacadeTest {

    @InjectMockKs
    private lateinit var couponFacade: CouponFacade

    @MockK(relaxed = true)
    private lateinit var couponService: CouponService

    @MockK(relaxed = true)
    private lateinit var couponSourceService: CouponSourceService

    @MockK(relaxed = true)
    private lateinit var userService: UserService

    @Test
    fun `getAllIssuableSources - 발급 가능 쿠폰 조회`() {
        val couponSources = List(3) { CouponMock.source(id = CouponMock.sourceId()) }
        every { couponSourceService.getAllIssuable() } returns couponSources

        val result = couponFacade.getAllSourcesIssuable()

        assertThat(result.size).isEqualTo(couponSources.size)
        couponSources.forEachIndexed { index, couponSource ->
            assertThat(result[index].id).isEqualTo(couponSource.id)
        }
        verify {
            couponSourceService.getAllIssuable()
        }
    }

    @Test
    fun `getCoupons - 사용자 쿠폰 조회`() {
        val userId = UserMock.id()
        val user = UserMock.user(id = userId)
        val coupons = List(3) { CouponMock.coupon(id = CouponMock.id()) }
        every { userService.get(userId.value) } returns user
        every { couponService.getAllUnused(userId) } returns coupons

        val result = couponFacade.getCoupons(userId.value)

        assertThat(result).hasSize(coupons.size)
        coupons.forEachIndexed { index, coupon ->
            assertThat(result[index].id).isEqualTo(coupon.id)
        }
        verify {
            userService.get(userId.value)
            couponService.getAllUnused(userId)
        }
    }
}
