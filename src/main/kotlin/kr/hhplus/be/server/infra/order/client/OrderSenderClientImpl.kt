package kr.hhplus.be.server.infra.order.client

import kr.hhplus.be.server.domain.order.OrderSender
import kr.hhplus.be.server.domain.order.OrderView
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
@EnableConfigurationProperties(OrderSenderClientProperty::class)
class OrderSenderClientImpl(
    private val webClientBuilder: WebClient.Builder,
    private val property: OrderSenderClientProperty,
) : OrderSender {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder
        .baseUrl(property.baseUrl)
        .build()

    override fun send(order: OrderView) {
        webClient
            .post()
            .uri(PATH)
            .bodyValue(
                mapOf(
                    "id" to order.id.value,
                )
            )
            .retrieve()
            .bodyToMono<Void>()
            .doOnSuccess {
                logger.info("[OrderSenderClient] 주문 전송 성공 ${order.id}")
            }
            .doOnError {
                logger.error("[OrderSenderClient] 주문 전송 실패 ${it.message}", it)
            }
            .subscribe()
    }

    companion object {
        const val PATH = "/orders/completed"
    }
}
