package kr.hhplus.be.server.interfaces.coupon.response

import kr.hhplus.be.server.domain.coupon.CouponQueryModel
import kr.hhplus.be.server.mock.CouponMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CouponsResponseTest {
    @Test
    fun `내 쿠폰 목록에 대한 응답 생성`() {
        val coupons = List(3) { CouponMock.couponQueryModel() }

        val response = CouponsResponse.from(coupons)

        assertThat(response.coupons).hasSize(coupons.size)
        response.coupons.forEachIndexed { index, couponResponse ->
            assertThat(couponResponse.id).isEqualTo(coupons[index].id.value)
            assertThat(couponResponse.userId).isEqualTo(coupons[index].userId.value)
            assertThat(couponResponse.name).isEqualTo(coupons[index].name)
            assertThat(couponResponse.discountAmount).isEqualByComparingTo(coupons[index].discountAmount)
            assertThat(couponResponse.usedAt).isEqualTo(coupons[index].usedAt)
            assertThat(couponResponse.createdAt).isEqualTo(coupons[index].createdAt)
            assertThat(couponResponse.updatedAt).isEqualTo(coupons[index].updatedAt)
        }

    }

    @Test
    fun `빈 리스트인 경우 빈 리스트 응답`() {
        val coupons = emptyList<CouponQueryModel>()

        val response = CouponsResponse.from(coupons)

        assertThat(response.coupons).isEmpty()
    }
}

