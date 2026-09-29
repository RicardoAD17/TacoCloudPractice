package tacos.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import tacos.messaging.contract.OrderEvent;
import tacos.messaging.contract.OrderMessagingService;

@Configuration
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "noop", matchIfMissing = true)
@Profile("!prod") // Criterio: NOOP nunca debe activarse silenciosamente en producción
public class NoopMessagingConfig {
    
    @Bean
    public OrderMessagingService orderMessagingService() {
        return new OrderMessagingService() {
            @Override
            public void sendOrderEvent(OrderEvent event) {
                System.out.println("NOOP ADAPTER: Simulación de envío del evento: " + event.getEventType());
            }
        };
    }
}