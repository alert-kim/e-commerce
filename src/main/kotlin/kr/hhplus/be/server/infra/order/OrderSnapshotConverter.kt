package kr.hhplus.be.server.infra.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kr.hhplus.be.server.domain.order.OrderSnapshot
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Converter
@Component
class OrderSnapshotConverter : AttributeConverter<OrderSnapshot, String> {
    @Autowired
    lateinit var objectMapper: ObjectMapper
    
    override fun convertToDatabaseColumn(attribute: OrderSnapshot): String {
        return objectMapper.writeValueAsString(attribute)
    }
    
    override fun convertToEntityAttribute(dbData: String): OrderSnapshot {
        return objectMapper.readValue(dbData)
    }
}
