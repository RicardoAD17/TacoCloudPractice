package tacos.messaging;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tacos.OutboxEvent;
import tacos.data.OutboxEventRepository;
import tacos.messaging.contract.OrderMessagingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import tacos.messaging.contract.OrderEvent;

import java.util.Date;

@Component
public class OutboxRelayPublisher {

    private final OutboxEventRepository outboxRepository;
    private final OrderMessagingService messagingService;
    private final ObjectMapper objectMapper;
    private final int MAX_ATTEMPTS = 3;
    @Scheduled(fixedDelayString = "${tacocloud.outbox.relay-interval:5000}")
    public void processOutbox() {
        outboxRepository.findByStatusIn("NEW", "FAILED")
            .flatMap(event -> {
                if (event.getAttempts() >= MAX_ATTEMPTS) {
                    return reactor.core.publisher.Mono.empty(); 
                }
                
                event.setStatus("PUBLISHING");
                event.setUpdatedAt(new Date());
                return outboxRepository.save(event);
            })
            .doOnNext(event -> {
                try {
                    OrderEvent orderEvent = objectMapper.readValue(event.getPayload(), OrderEvent.class);
                    messagingService.sendOrderEvent(orderEvent);
                    event.setStatus("PUBLISHED");
                } catch (Exception e) {

                    event.setStatus("FAILED");
                    event.setAttempts(event.getAttempts() + 1);
                }
                event.setUpdatedAt(new Date());
                outboxRepository.save(event).subscribe(); 
            })
            .subscribe();
    }
}