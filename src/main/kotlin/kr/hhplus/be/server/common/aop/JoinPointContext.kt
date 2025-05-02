package kr.hhplus.be.server.common.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.lang.reflect.Method

class JoinPointContext(private val joinPoint: ProceedingJoinPoint) {
    val signature: MethodSignature by lazy { joinPoint.signature as MethodSignature }
    val method: Method by lazy { signature.method }
    val parameterNames: Array<String> by lazy { signature.parameterNames }
    val arguments: Array<Any?> by lazy { joinPoint.args }

    inline fun <reified T : Annotation> getAnnotation(): T =
        getAnnotation(T::class.java)

    fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T =
        method.getAnnotation(annotationClass)
            ?: throw IllegalStateException("${annotationClass.simpleName} annotation not found")

    fun proceed(): Any? =
        joinPoint.proceed()
}
