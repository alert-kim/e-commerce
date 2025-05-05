package kr.hhplus.be.server.client

import kr.hhplus.be.server.domain.order.OrderSnapshotClient
import kr.hhplus.be.server.domain.order.OrderSnapshot
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
@EnableConfigurationProperties(OrderSnapshotClientProperty::class)
class OrderSnapshotClientImpl(
    private val webClientBuilder: WebClient.Builder,
    private val property: OrderSnapshotClientProperty,
) : OrderSnapshotClient {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder
        .baseUrl(property.baseUrl)
        .build()

    override fun send(snapshot: OrderSnapshot) {
        webClient
            .post()
            .uri(PATH)
            .bodyValue(mapOf(
                "id" to snapshot.id.value,
            ))
            .retrieve()
            .bodyToMono<Void>()
            .doOnSuccess {
                logger.info("[OrderSnapshotClient] 주문 전송 성공 ${snapshot.id}")
            }
            .doOnError {
                logger.error("[OrderSnapshotClient] 주문 전송 실패 ${it.message}", it)
            }
            .subscribe()
    }

    companion object {
        const val PATH = "/orders/completed"
    }
}
