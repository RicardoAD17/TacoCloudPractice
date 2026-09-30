package tacos.service;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tacos.data.OutboxEventRepository;

@Component("outbox")
public class OutboxHealthIndicator implements ReactiveHealthIndicator {

    private final OutboxEventRepository outboxRepo;

    public OutboxHealthIndicator(OutboxEventRepository outboxRepo) {
        this.outboxRepo = outboxRepo;
    }

    @Override
    public Mono<Health> health() {
        return outboxRepo.count()
                .map(total -> Health.up()
                        .withDetail("message", "Outbox DB connection is UP")
                        .withDetail("totalEventsHistorical", total)
                        .build())
                .onErrorResume(ex -> {
                    Exception exception = ex instanceof Exception ? (Exception) ex : new RuntimeException(ex);
                    
                    return Mono.just(Health.down(exception)
                            .withDetail("message", "Fallo al conectar con la colección del Outbox")
                            .build());
                });
    }
}