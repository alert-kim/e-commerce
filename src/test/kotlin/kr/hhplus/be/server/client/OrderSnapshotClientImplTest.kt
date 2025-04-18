package kr.hhplus.be.server.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.spyk
import kr.hhplus.be.server.domain.order.dto.OrderSnapshot
import kr.hhplus.be.server.mock.OrderMock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderSnapshotClientImplTest {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = jacksonObjectMapper().registerModules(JavaTimeModule())

    private val server by lazy {
        WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort()).apply {
            start()
            addMockServiceRequestListener { request, response ->
                logger.info("[Mock] ${request.url} ${request.bodyAsString}")
            }
        }
    }

    private val orderSnapshotClient = spyk(
        OrderSnapshotClientImpl(
            webClientBuilder = WebClient.builder(),
            property = OrderSnapshotClientProperty(
                baseUrl = server.baseUrl(),
            ),
        ),
    )

    @Test
    fun `주문 스냅샷이 정상적으로 전송된다`() {
        server.stubFor(
            post(OrderSnapshotClientImpl.PATH)
                .willReturn(ok()),
        )

        val orderSnapshot = OrderSnapshot.from(OrderMock.order())
        orderSnapshotClient.send(orderSnapshot)

        Thread.sleep(1_000)

        server.verify(
            postRequestedFor(urlPathEqualTo(OrderSnapshotClientImpl.PATH))
                .withRequestBody(equalToJson(mapper.writeValueAsString(orderSnapshot)))
        )
    }
}
