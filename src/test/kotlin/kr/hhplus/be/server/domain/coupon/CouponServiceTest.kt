package kr.hhplus.be.server.domain.coupon

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.domain.coupon.command.CancelCouponUseCommand
import kr.hhplus.be.server.domain.coupon.command.CreateCouponCommand
import kr.hhplus.be.server.domain.coupon.command.UseCouponCommand
import kr.hhplus.be.server.domain.coupon.exception.NotFoundCouponException
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.result.UsedCoupon
import kr.hhplus.be.server.testutil.mock.CouponMock
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class CouponServiceTest {
    
    private val repository = mockk<CouponRepository>(relaxed = true)
    private lateinit var service: CouponService

    @BeforeEach
    fun setUp() {
        clearMocks(repository)
        service = CouponService(repository)
    }

    @Nested
    @DisplayName("쿠폰 생성")
    inner class CreateCoupon {
        @Test
        @DisplayName("쿠폰 생성 성공")
        fun create() {
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
    }

    @Nested
    @DisplayName("쿠폰 사용")
    inner class UseCoupon {
        @Test
        @DisplayName("사용 성공")
        fun use() {
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
        @DisplayName("쿠폰 없음")
        fun notFound() {
            val couponId = CouponMock.id()
            val userId = UserMock.id()
            every { repository.findById(couponId.value) } returns null

            assertThrows<NotFoundCouponException> {
                service.use(UseCouponCommand(couponId.value, userId))
            }
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 취소")
    inner class CancelCouponUse {
        @Test
        @DisplayName("취소 성공")
        fun cancel() {
            val couponId = CouponMock.id()
            val coupon = mockk<Coupon>(relaxed = true)
            every { repository.findById(couponId.value) } returns coupon
            
            service.cancelUse(CancelCouponUseCommand(couponId.value))
            
            verify {
                repository.findById(couponId.value)
                coupon.cancelUse()
            }
        }

        @Test
        @DisplayName("쿠폰 없음")
        fun notFound() {
            val couponId = CouponMock.id()
            every { repository.findById(couponId.value) } returns null
            
            assertThrows<NotFoundCouponException> {
                service.cancelUse(CancelCouponUseCommand(couponId.value))
            }
        }
    }

    @Nested
    @DisplayName("미사용 쿠폰 조회")
    inner class GetUnusedCoupons {
        @Test
        @DisplayName("미사용 쿠폰 목록 조회")
        fun getUnused() {
            val userId = UserMock.id()
            val coupons = List(3) { CouponMock.coupon(id = CouponMock.id(), userId = userId) }
            every { repository.findAllByUserIdAndUsedAtIsNull(userId) } returns coupons

            val result = service.getAllUnused(userId)

            assertThat(result).hasSize(3)
            verify {
                repository.findAllByUserIdAndUsedAtIsNull(userId)
            }
        }
        
        @Test
        @DisplayName("미사용 쿠폰 없음")
        fun empty() {
            val userId = UserMock.id()
            every { repository.findAllByUserIdAndUsedAtIsNull(userId) } returns emptyList<Coupon>()

            val result = service.getAllUnused(userId)

            assertThat(result).isEmpty()
            verify {
                repository.findAllByUserIdAndUsedAtIsNull(userId)
            }
        }
    }
}
