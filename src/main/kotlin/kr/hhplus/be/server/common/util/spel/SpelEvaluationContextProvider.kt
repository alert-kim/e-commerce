package kr.hhplus.be.server.common.util.spel

import kr.hhplus.be.server.common.aop.JoinPointContext
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.support.StandardEvaluationContext

object SpelEvaluationContextProvider {

    fun fromJoinPoint(joinPoint: JoinPointContext): EvaluationContext {
        val context = StandardEvaluationContext()
        val parameterNames = joinPoint.parameterNames
        val arguments = joinPoint.arguments

        parameterNames.forEachIndexed { i, paramName ->
            context.setVariable(paramName, arguments[i])
        }

        return context
    }
}
