package tacos.kitchen;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tacos.ProcessedEvent;
import tacos.data.ProcessedEventRepository;
import tacos.messaging.contract.OrderEvent;

@Component
public class OrderListener {

    private final ProcessedEventRepository processedEventRepo;
    private final OrderReceiver orderReceiver;

    public OrderListener(ProcessedEventRepository processedEventRepo, OrderReceiver orderReceiver) {
        this.processedEventRepo = processedEventRepo;
        this.orderReceiver = orderReceiver;
    }

    @RabbitListener(queues = "tacocloud.order.queue") 
    public void handleOrderCreated(OrderEvent event) {
        processedEventRepo.findByEventId(event.getEventId())
            .flatMap(existing -> {
               
                System.out.println("Evento duplicado detectado en RabbitMQ, omitiendo procesamiento: " + event.getEventId());
                return Mono.empty();
            })
            .switchIfEmpty(
               
                processedEventRepo.save(new ProcessedEvent(event.getEventId(), event.getEventType().name(), "PROCESSING"))
                    .flatMap(savedEvent -> {
                       
                        orderReceiver.receiveOrder(); 
                        
                        savedEvent.setResult("SUCCESS");
                        return processedEventRepo.save(savedEvent);
                    })
                    .onErrorResume(error -> {
                        
                        return processedEventRepo.findByEventId(event.getEventId())
                            .flatMap(failedEvent -> {
                                failedEvent.setResult("FAILED");
                                return processedEventRepo.save(failedEvent);
                            })
                            .then(Mono.error(error));
                    })
            )
            .block(); 
    }
}