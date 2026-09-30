package tacos.messaging;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import tacos.messaging.contract.OrderEvent; 
import tacos.messaging.contract.OrderMessagingService;
@Service
@Primary 
public class RabbitOrderMessagingService implements OrderMessagingService {
  
  private final RabbitTemplate rabbit;
  private final String destination;
  
  public RabbitOrderMessagingService(
          RabbitTemplate rabbit, 
          @Value("${tacocloud.messaging.destination.order-events:tacocloud.orders.topic}") String destination) {
    this.rabbit = rabbit;
    this.destination = destination;
  }
  
  @Override 
  public void sendOrderEvent(OrderEvent event) {
    rabbit.convertAndSend(destination, event,
        new MessagePostProcessor() {
          @Override
          public Message postProcessMessage(Message message) throws AmqpException {
            MessageProperties props = message.getMessageProperties();
            props.setHeader("X_ORDER_SOURCE", "WEB");
            return message;
          } 
        });
  }
}