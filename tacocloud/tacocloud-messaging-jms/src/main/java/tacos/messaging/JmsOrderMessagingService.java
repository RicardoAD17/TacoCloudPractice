package tacos.messaging;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import tacos.messaging.contract.OrderEvent; 
import tacos.messaging.contract.OrderMessagingService; 
import org.springframework.context.annotation.Profile;
@Service
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "jms")
@Profile("jms")
public class JmsOrderMessagingService implements OrderMessagingService {

  private final JmsTemplate jms;

  @Autowired
  public JmsOrderMessagingService(JmsTemplate jms) {
    this.jms = jms;
  }

  @Override
  public void sendOrderEvent(OrderEvent event) {
    jms.convertAndSend("tacocloud.order.queue", event,
        this::addOrderSource);
  }
  
  private Message addOrderSource(Message message) throws JMSException {
    message.setStringProperty("X_ORDER_SOURCE", "WEB");
    return message;
  }
}