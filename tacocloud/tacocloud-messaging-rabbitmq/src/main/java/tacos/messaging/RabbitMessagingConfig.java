package tacos.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tacos.messaging.contract.OrderEvent;
import tacos.messaging.contract.OrderMessagingService;

@Configuration
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "rabbit") 
public class RabbitMessagingConfig {

   
    @Bean
    @Primary
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

   
    @Bean("rabbitOrderMessagingService")
    @Primary 
    public OrderMessagingService orderMessagingService(RabbitTemplate rabbit) {
        return new OrderMessagingService() {
            @Override
            public void sendOrderEvent(OrderEvent event) {
                rabbit.convertAndSend("tacocloud.orders.topic", event);
            }
        };
    }
}