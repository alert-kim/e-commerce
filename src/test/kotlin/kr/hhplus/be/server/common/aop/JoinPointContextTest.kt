package kr.hhplus.be.server.common.aop

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.Method

class JoinPointContextTest {

    @Test
    @DisplayName("signature는 JoinPoint의 signature")
    fun getSignature() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature

        val signature = context.signature

        verify(exactly = 1) { joinPoint.signature }
        assertThat(signature).isSameAs(methodSignature)
    }

    @Test
    @DisplayName("method는 MethodSignature의 method")
    fun getMethod() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val method = mockk<Method>()
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.method } returns method

        val actual = context.method

        verify(exactly = 1) { methodSignature.method }
        assertThat(actual).isSameAs(method)
    }

    @Test
    @DisplayName("parameterNames는 MethodSignature의 parameterNames")
    fun getParameterNames() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val parameterNames = arrayOf("param1", "param2")
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.parameterNames } returns parameterNames

        val actual = context.parameterNames

        verify(exactly = 1) { methodSignature.parameterNames }
        assertThat(actual).isEqualTo(parameterNames)
    }

    @Test
    @DisplayName("arguments는 JoinPoint의 args")
    fun getArguments() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val args = arrayOf("arg1", 123)
        val context = JoinPointContext(joinPoint)
        every { joinPoint.args } returns args

        val actual = context.arguments

        verify(exactly = 1) { joinPoint.args }
        assertThat(actual).isEqualTo(args)
    }

    @Test
    @DisplayName("메소드에서 해당하는 애노테이션을 조회")
    fun getAnnotation() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val method = mockk<Method>()
        val annotation = mockk<TestAnnotation>()
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.method } returns method
        every { method.getAnnotation(TestAnnotation::class.java) } returns annotation

        val result = context.getAnnotation(TestAnnotation::class.java)

        assertThat(result).isSameAs(annotation)
    }

    @Test
    @DisplayName("reified 타입 파라미터를 사용하여 애노테이션을 조회")
    fun getAnnotationWithReifiedType() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val method = mockk<Method>()
        val annotation = mockk<TestAnnotation>()
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.method } returns method
        every { method.getAnnotation(TestAnnotation::class.java) } returns annotation

        val result = context.getAnnotation<TestAnnotation>()

        assertThat(result).isSameAs(annotation)
    }

    @Test
    @DisplayName("애노테이션 조회시 애노테이션이 없으면 예외 발생")
    fun getNotExistsAnnotation() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val method = mockk<Method>()
        every { joinPoint.signature } returns methodSignature
        every { methodSignature.method } returns method
        every { method.getAnnotation(TestAnnotation::class.java) } returns null

        val context = JoinPointContext(joinPoint)

        assertThrows<IllegalStateException>() {
            context.getAnnotation(TestAnnotation::class.java)
        }
    }

    @Test
    @DisplayName("proceed를 호출하면 JoinPoint의 proceed를 호출")
    fun proceed() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val methodSignature = mockk<MethodSignature>()
        val result = "result"
        val context = JoinPointContext(joinPoint)
        every { joinPoint.signature } returns methodSignature
        every { joinPoint.proceed() } returns result

        val actual = context.proceed()

        verify(exactly = 1) {
            joinPoint.proceed()
        }
        assertThat(actual).isEqualTo(result)
    }

    @Retention(AnnotationRetention.RUNTIME)
    annotation class TestAnnotation
}
