package tacos.web.api; // o el paquete donde decidas ponerlo

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tacos.data.OutboxEventRepository;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OutboxMetricsService {

    private final OutboxEventRepository outboxRepo;
    private final AtomicInteger pendingOutboxCount = new AtomicInteger(0);

    public OutboxMetricsService(OutboxEventRepository outboxRepo, MeterRegistry meterRegistry) {
        this.outboxRepo = outboxRepo;
        
        meterRegistry.gauge("tacocloud.outbox.pending", pendingOutboxCount);
    }
    @Scheduled(fixedRate = 5000)
    public void updatePendingCount() {
               outboxRepo.countByStatus("PENDING")
                .subscribe(count -> pendingOutboxCount.set(count.intValue()));
    }
}