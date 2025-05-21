package kr.hhplus.be.server.infra.order.client

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "order.snapshot.client")
data class OrderSnapshotClientProperty(
    val baseUrl: String,
)
