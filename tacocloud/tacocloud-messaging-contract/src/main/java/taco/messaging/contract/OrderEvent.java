package tacos.messaging.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) 
public class OrderEvent {
    private String eventId = UUID.randomUUID().toString(); 
    private String correlationId;
    private OrderEventType eventType;
    private String version = "v1";
    private Date occurredAt = new Date();
    
    private OrderEventPayload payload;
}