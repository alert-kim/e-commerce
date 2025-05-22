package kr.hhplus.be.server.application.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CouponFacadeTest {
    private val couponService = mockk<CouponService>(relaxed = true)
    private val couponSourceService = mockk<CouponSourceService>(relaxed = true)
    private val userService = mockk<UserService>(relaxed = true)
    private val couponFacade = CouponFacade(couponService, couponSourceService, userService)

    @BeforeEach
    fun setup() {
        clearMocks(couponService, couponSourceService, userService)
    }

    @Nested
    @DisplayName("쿠폰 발급")
    inner class IssueCoupon {
        @Test
        @DisplayName("사용자 ID와 쿠폰 소스 ID로 쿠폰을 발급한다")
        fun issueCoupon() {
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

            assertThat(result.coupon.id).isEqualTo(coupon.id)
            verify {
                userService.get(userId.value)
                couponSourceService.issue(IssueCouponCommand(couponSourceId.value))
                couponService.create(CreateCouponCommand(userId, issuedCoupon))
            }
        }
    }

    @Nested
    @DisplayName("사용 가능한 쿠폰 조회")
    inner class GetUsableCoupons {
        @Test
        @DisplayName("사용자 ID로 사용 가능한 쿠폰 목록을 조회한다")
        fun getUserCoupons() {
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
    }

    @Nested
    @DisplayName("발급 가능한 쿠폰 소스 조회")
    inner class GetAllSourcesIssuable {
        @Test
        @DisplayName("발급 가능한 모든 쿠폰 소스를 조회한다")
        fun getSources() {
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
}
