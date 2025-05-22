package kr.hhplus.be.server.infra.order.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "order.sender.client")
data class OrderSenderClientProperty(
    val baseUrl: String,
)
