package kr.hhplus.be.server.util.spec

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.common.aop.JoinPointContext
import kr.hhplus.be.server.common.util.spel.SpelEvaluationContextProvider
import kr.hhplus.be.server.testutil.mock.UserMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SpelEvaluationContextProviderTest {
    @Test
    @DisplayName("JoinPointContext로부터 파라미터 이름과 값을 가진 컨텍스트를 생성")
    fun createContextFromJoinPointContext() {
        val joinPointContext = mockk<JoinPointContext>()
        val user = UserMock.view()
        every { joinPointContext.parameterNames } returns arrayOf("user", "isTest")
        every { joinPointContext.arguments } returns arrayOf(user, true)

        val context = SpelEvaluationContextProvider.fromJoinPoint(joinPointContext)

        assertThat(context.lookupVariable("user")).isEqualTo(user)
        assertThat(context.lookupVariable("isTest")).isEqualTo(true)
        assertThat(context.lookupVariable("absent")).isNull()
    }
}
