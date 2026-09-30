package tacos.messaging.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderEventContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serialize_DoesNotContainSensitiveData_AndMaintainsFormat() throws Exception {
        OrderEventPayload payload = new OrderEventPayload();
        payload.setOrderId("order-123");
        payload.setDeliveryName("Ricardo Almada");
        payload.setStatus("CREATED");
        payload.setTacoNames(Arrays.asList("Carnivore", "Veg-Out"));
        OrderEvent event = new OrderEvent();
        event.setCorrelationId(UUID.randomUUID().toString());
        event.setEventType(OrderEventType.CREATED);
        event.setPayload(payload);
        String json = objectMapper.writeValueAsString(event);
        assertTrue(json.contains("\"eventId\""), "Debe contener eventId");
        assertTrue(json.contains("\"version\":\"v1\""), "Debe definir la version v1");
        assertTrue(json.contains("\"correlationId\""), "Debe contener correlationId");
        
        assertFalse(json.contains("ccNumber"), "¡Alerta de seguridad! Se filtró una tarjeta");
        assertFalse(json.contains("password"), "¡Alerta de seguridad! Se filtró un password");
    }

    @Test
    public void deserialize_IgnoresUnknownFields_ForForwardCompatibility() throws Exception {
        String futureJson = "{" +
                "\"eventId\":\"uuid-1234\"," +
                "\"eventType\":\"ORDER_CREATED\"," +
                "\"version\":\"v2\"," +
                "\"newV2Field\":\"Esto rompería a un consumidor estricto\"," +
                "\"payload\":{\"orderId\":\"order-123\"}" +
                "}";
        OrderEvent parsedEvent = objectMapper.readValue(futureJson, OrderEvent.class);

        assertEquals("uuid-1234", parsedEvent.getEventId());
        assertEquals(OrderEventType.CREATED, parsedEvent.getEventType());
        assertEquals("v2", parsedEvent.getVersion()); 
        assertNotNull(parsedEvent.getPayload());
    }
}