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
    fun `use - 쿠폰 사용`() {
        val couponId = CouponMock.id()
        val coupon = CouponMock.coupon(id = couponId, usedAt = null)
        every { repository.findById(couponId.value) } returns coupon

        val result = service.use(UseCouponCommand(couponId.value, coupon.userId))

        assertThat(result.value.id).isEqualTo(coupon.id)
        assertThat(result.value.usedAt).isNotNull()
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
}
