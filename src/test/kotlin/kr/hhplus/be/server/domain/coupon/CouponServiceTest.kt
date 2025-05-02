package kr.hhplus.be.server.domain.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class CouponServiceTest {
    @InjectMockKs
    private lateinit var service: CouponService

    @MockK(relaxed = true)
    private lateinit var repository: CouponRepository

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
    }

    @Test
    fun `create - 쿠폰 생성`() {
        val userId = UserMock.id()
        val issuedCoupon = CouponMock.issuedCoupon()
        val coupon = CouponMock.coupon(id = CouponMock.id(), userId = userId)
        every { repository.save(any()) } returns coupon

        val result = service.create(CreateCouponCommand(userId, issuedCoupon))

        assertThat(result).isEqualTo(CouponView.from(coupon))
        verify {
            repository.save(withArg {
                assertThat(it.userId).isEqualTo(userId)
                assertThat(it.couponSourceId).isEqualTo(issuedCoupon.couponSourceId)
                assertThat(it.name).isEqualTo(issuedCoupon.name)
                assertThat(it.discountAmount).isEqualByComparingTo(issuedCoupon.discountAmount)
                assertThat(it.createdAt).isEqualTo(issuedCoupon.createdAt)
            })
        }
    }

    @Test
    fun `use - 쿠폰 사용`() {
        val couponId = CouponMock.id()
        val coupon = mockk<Coupon>()
        val userId = UserMock.id()
        val usedCoupon = UsedCoupon(
            id = CouponMock.id(),
            userId = UserMock.id(),
            discountAmount = 1000.toBigDecimal(),
            usedAt = Instant.now(),
        )
        every { repository.findById(couponId.value) } returns coupon
        every { coupon.use(any()) } returns usedCoupon
        val result = service.use(UseCouponCommand(couponId.value, userId))

        assertThat(result).isEqualTo(usedCoupon)
        verify {
            repository.findById(couponId.value)
            coupon.use(userId)
        }
    }

    @Test
    fun `use - 찾을 수 없는 쿠폰 - NotFoundCouponException`() {
        val couponId = CouponMock.id()
        val userId = UserMock.id()
        every { repository.findById(couponId.value) } returns null

        assertThrows<NotFoundCouponException> {
            service.use(UseCouponCommand(couponId.value, userId))
        }
    }

    @Test
    fun `getUnused - 해당 유저의 usedAt이 null인 쿠폰 목록 조회`() {
        val userId = UserMock.id()
        val coupons = List(3) { CouponMock.coupon(id = CouponMock.id(), userId = userId) }
        every { repository.findAllByUserIdAndUsedAtIsNull(userId) } returns coupons

        service.getAllUnused(userId)

        verify {
            repository.findAllByUserIdAndUsedAtIsNull(userId)
        }
    }
}
