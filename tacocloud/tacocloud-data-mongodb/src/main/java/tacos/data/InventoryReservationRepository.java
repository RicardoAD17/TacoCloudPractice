package tacos.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import tacos.InventoryReservation;

public interface InventoryReservationRepository extends ReactiveCrudRepository<InventoryReservation, String> {
    Mono<InventoryReservation> findByOrderId(String orderId);
    Mono<InventoryReservation> findByIdempotencyKey(String idempotencyKey);
}