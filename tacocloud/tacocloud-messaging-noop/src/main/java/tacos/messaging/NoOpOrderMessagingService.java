package tacos.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tacos.messaging.contract.OrderEvent;
import tacos.messaging.contract.OrderMessagingService;

@Service
@ConditionalOnProperty(name = "tacocloud.messaging.transport", havingValue = "noop", matchIfMissing = true)
@Profile("!prod") 
public class NoOpOrderMessagingService implements OrderMessagingService {
    @Override
    public void sendOrderEvent(OrderEvent event) {
        System.out.println("NOOP: Evento ignorado en ambiente dev/test.");
    }
}