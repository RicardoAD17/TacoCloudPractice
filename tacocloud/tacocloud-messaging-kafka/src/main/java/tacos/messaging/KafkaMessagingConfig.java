package tacos.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tacos.messaging.contract.OrderEvent;
import tacos.messaging.contract.OrderMessagingService;

@Configuration
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "kafka")
public class KafkaMessagingConfig {

    @Bean
    public OrderMessagingService orderMessagingService() {
        return new OrderMessagingService() {
            @Override
            public void sendOrderEvent(OrderEvent event) {
                System.out.println("KAFKA ADAPTER: Enviando mensaje por Kafka... (Simulado)");
            }
        };
    }
}