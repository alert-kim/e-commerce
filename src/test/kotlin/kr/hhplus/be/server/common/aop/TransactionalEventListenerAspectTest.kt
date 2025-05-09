package kr.hhplus.be.server.common.aop

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.hhplus.be.server.common.event.aspect.TransactionalEventListenerAspect
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runners.model.TestClass
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.Logger
import org.springframework.transaction.event.TransactionalEventListener
import java.lang.reflect.Method

@ExtendWith(MockitoExtension::class)
class TransactionalEventListenerAspectTest {

    @Test
    @DisplayName("로직 실행 성공시, 결과 반환")
    fun doRunCatchingAndLog_success() {
        val aspect = TransactionalEventListenerAspect()
        val joinPoint = mockk<ProceedingJoinPoint>(relaxed = true)
        val expected = "result"
        every { joinPoint.signature } returns mockk<MethodSignature>(relaxed = true)
        every { joinPoint.proceed() } returns expected

        val result = aspect.doRunCatchingAndLog(joinPoint)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("로직 실패 시 null 반환하고 로그 기록")
    fun doRunCatchingAndLog_failure() {
        val aspect = TransactionalEventListenerAspect()
        val joinPoint = mockk<ProceedingJoinPoint>(relaxed = true)
        val exception = RuntimeException("Test exception")
        every { joinPoint.signature } returns mockk<MethodSignature>(relaxed = true)
        every { joinPoint.proceed() } throws exception

        val result = aspect.doRunCatchingAndLog(joinPoint)

        assertThat(result).isNull()
    }
}
