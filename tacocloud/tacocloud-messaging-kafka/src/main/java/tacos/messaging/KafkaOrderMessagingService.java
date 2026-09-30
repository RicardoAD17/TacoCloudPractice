package tacos.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import tacos.messaging.contract.OrderEvent;
import tacos.messaging.contract.OrderMessagingService; 
import org.springframework.context.annotation.Profile;
@Service
@Profile("kafka")
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "kafka")
public class KafkaOrderMessagingService implements OrderMessagingService {

  private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

  @Autowired
  public KafkaOrderMessagingService(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void sendOrderEvent(OrderEvent event) {
    kafkaTemplate.send("tacocloud.orders.topic", event);
  }
  
}