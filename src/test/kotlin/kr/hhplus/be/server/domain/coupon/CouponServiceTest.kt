package kr.hhplus.be.server.domain.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.AlreadyUsedCouponException
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.mock.CouponMock
import kr.hhplus.be.server.mock.UserMock
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
        val couponId = CouponMock.id()
        every { repository.save(any()) } returns couponId

        val result = service.create(CreateCouponCommand(userId, issuedCoupon))

        assertThat(result.id).isEqualTo(couponId)
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.couponSourceId).isEqualTo(issuedCoupon.couponSourceId)
        assertThat(result.name).isEqualTo(issuedCoupon.name)
        assertThat(result.discountAmount).isEqualByComparingTo(issuedCoupon.discountAmount)
        assertThat(result.createdAt).isEqualTo(issuedCoupon.createdAt)
        verify {
            repository.save(withArg {
                assertThat(it.userId).isEqualTo(userId)
            })
        }
    }

    @Test
    fun `use - 쿠폰 사용`() {
        val couponId = CouponMock.id()
        val coupon = CouponMock.coupon(id = couponId, usedAt = null)
        every { repository.findById(couponId.value) } returns coupon

        val result = service.use(UseCouponCommand(couponId.value, coupon.userId))

        assertThat(result.id).isEqualTo(coupon.id)
        assertThat(result.usedAt).isNotNull()
        verify {
            repository.findById(couponId.value)
            repository.save(withArg {
                assertThat(it.usedAt).isNotNull()
            })
        }
    }

    @Test
    fun `use - 이미 사용한 쿠폰`() {
        val couponId = CouponMock.id()
        val coupon = CouponMock.coupon(id = couponId, usedAt = Instant.now())
        every { repository.findById(couponId.value) } returns coupon

        assertThrows<AlreadyUsedCouponException> {
            service.use(UseCouponCommand(couponId.value, coupon.userId))
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
