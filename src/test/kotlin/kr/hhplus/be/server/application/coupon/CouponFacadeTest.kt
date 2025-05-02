package kr.hhplus.be.server.application.coupon

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.application.coupon.command.IssueCouponFacadeCommand
import kr.hhplus.be.server.domain.coupon.CouponService
import kr.hhplus.be.server.domain.coupon.CouponSourceService
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.IssueCouponCommand
import kr.hhplus.be.server.domain.user.UserService
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
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
    fun `issueCoupon - 쿠폰 발급`() {
        val couponSourceId = CouponMock.sourceId()
        val userId = UserMock.id()
        val issuedCoupon = CouponMock.issuedCoupon()
        val coupon = CouponMock.view(id = CouponMock.id())
        every { couponSourceService.issue(any()) } returns issuedCoupon
        every { userService.get(userId.value) } returns UserMock.view(id = userId)
        every { couponService.create(any()) } returns coupon

        val result = couponFacade.issueCoupon(
            IssueCouponFacadeCommand(
                couponSourceId = couponSourceId.value,
                userId = userId.value
            )
        )

        assertThat(result.value.id).isEqualTo(coupon.id)
        verify {
            userService.get(userId.value)
            couponSourceService.issue(IssueCouponCommand(couponSourceId.value))
            couponService.create(CreateCouponCommand(userId, issuedCoupon))
        }
    }

    @Test
    fun `getUsableCoupons - 사용 가능한 사용자 쿠폰 조회`() {
        val userId = UserMock.id()
        val user = UserMock.view(id = userId)
        val coupons = List(3) { CouponMock.view(id = CouponMock.id()) }
        every { userService.get(userId.value) } returns user
        every { couponService.getAllUnused(userId) } returns coupons

        val result = couponFacade.getUsableCoupons(userId.value)

        assertThat(result.value).hasSize(coupons.size)
        coupons.forEachIndexed { index, coupon ->
            assertThat(result.value[index].id).isEqualTo(coupon.id)
        }
        verify {
            userService.get(userId.value)
            couponService.getAllUnused(userId)
        }
    }

    @Test
    fun `getAllSourcesIssuable - 발급 가능한 쿠폰 소스 조회`() {
        val couponSources = List(3) { CouponMock.sourceView() }
        every { couponSourceService.getAllIssuable() } returns couponSources

        val result = couponFacade.getAllSourcesIssuable()

        assertThat(result.value).hasSize(couponSources.size)
        couponSources.forEachIndexed { index, source ->
            assertThat(result.value[index].id).isEqualTo(source.id)
        }
        verify { couponSourceService.getAllIssuable() }
    }
}
