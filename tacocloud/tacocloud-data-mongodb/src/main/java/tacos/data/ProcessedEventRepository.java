package tacos.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tacos.ProcessedEvent;

public interface ProcessedEventRepository extends ReactiveCrudRepository<ProcessedEvent, String> {

    Mono<ProcessedEvent> findByEventId(String eventId);
    
}