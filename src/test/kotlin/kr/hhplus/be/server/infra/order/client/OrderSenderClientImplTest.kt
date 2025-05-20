package kr.hhplus.be.server.infra.order.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.spyk
import kr.hhplus.be.server.domain.order.OrderView
import kr.hhplus.be.server.testutil.mock.OrderMock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderSenderClientImplTest {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val server by lazy {
        WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply {
            start()
            addMockServiceRequestListener { request, response ->
                logger.info("[Mock] ${request.url} ${request.bodyAsString}")
            }
        }
    }

    private val orderSenderClient = spyk(
        OrderSenderClientImpl(
            webClientBuilder = WebClient.builder(),
            property = OrderSenderClientProperty(
                baseUrl = server.baseUrl(),
            ),
        ),
    )

    @Test
    @DisplayName("주문 스냅샷을 전송한다")
    fun send() {
        server.stubFor(
            WireMock.post(OrderSenderClientImpl.PATH)
                .willReturn(WireMock.ok()),
        )

        val order = OrderView.from(OrderMock.order())
        orderSenderClient.send(order)

        Thread.sleep(1_000)

        server.verify(
            WireMock.postRequestedFor(WireMock.urlPathEqualTo(OrderSenderClientImpl.PATH))
                .withRequestBody(WireMock.equalToJson("""{"id":${order.id.value}}"""))
        )
    }
}
