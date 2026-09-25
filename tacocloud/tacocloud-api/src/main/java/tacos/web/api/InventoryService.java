package tacos.web.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.InventoryReservation;
import tacos.Taco;
import tacos.TacoOrder;
import tacos.data.InventoryReservationRepository;

@Service
public class InventoryService {

    private final ReactiveMongoTemplate mongoTemplate;
    private final InventoryReservationRepository reservationRepo;

    public InventoryService(ReactiveMongoTemplate mongoTemplate, InventoryReservationRepository reservationRepo) {
        this.mongoTemplate = mongoTemplate;
        this.reservationRepo = reservationRepo;
    }

    public Mono<InventoryReservation> reserveStock(TacoOrder order, String idempotencyKey) {
        return reservationRepo.findByIdempotencyKey(idempotencyKey)
                .switchIfEmpty(Mono.defer(() -> executeReservationFlow(order, idempotencyKey)));
    }

    private Mono<InventoryReservation> executeReservationFlow(TacoOrder order, String idempotencyKey) {
        
        java.util.Map<String, Integer> requiredStock = new java.util.HashMap<>();
        if (order.getTacos() != null) {
            for (Taco taco : order.getTacos()) {
                int tacoQty = taco.getQuantity() > 0 ? taco.getQuantity() : 1;
                if (taco.getIngredients() != null) {
                    for (Ingredient ing : taco.getIngredients()) {
                        requiredStock.put(ing.getId(), requiredStock.getOrDefault(ing.getId(), 0) + tacoQty);
                    }
                }
            }
        }

    
        List<Map.Entry<String, Integer>> sortedIngredients = new ArrayList<>(requiredStock.entrySet());
       sortedIngredients.sort(Comparator.comparing(Map.Entry::getKey));

        List<InventoryReservation.ReservedItem> successfullyReserved = new ArrayList<>();

        return Flux.fromIterable(sortedIngredients)
                .concatMap(entry -> {
                    String ingId = entry.getKey();
                    int qty = entry.getValue();

                    Query query = new Query(Criteria.where("id").is(ingId).and("stockOnHand").gte(qty));
                    Update update = new Update().inc("stockOnHand", -qty);

                    return mongoTemplate.updateFirst(query, update, Ingredient.class)
                            .flatMap(result -> {
                                if (result.getModifiedCount() > 0) {
                                    successfullyReserved.add(new InventoryReservation.ReservedItem(ingId, qty));
                                    return Mono.empty();
                                } else {
                                   
                                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK: " + ingId));
                                }
                            });
                })
                .then(Mono.defer(() -> {
                    InventoryReservation reservation = new InventoryReservation();
                    reservation.setOrderId(order.getId());
                    reservation.setIdempotencyKey(idempotencyKey);
                    reservation.setItems(successfullyReserved);
                    reservation.setStatus(InventoryReservation.Status.RESERVED);
                    return reservationRepo.save(reservation);
                }))
                .onErrorResume(error -> {
                    
                    return Flux.fromIterable(successfullyReserved)
                            .concatMap(item -> {
                                Query rollbackQuery = new Query(Criteria.where("id").is(item.getIngredientId()));
                                Update rollbackUpdate = new Update().inc("stockOnHand", item.getQuantity());
                                return mongoTemplate.updateFirst(rollbackQuery, rollbackUpdate, Ingredient.class);
                            })
                            .then(Mono.error(error)); 
                });
    }

    public Mono<Void> releaseStock(String orderId) {
        return reservationRepo.findByOrderId(orderId)
                .filter(res -> res.getStatus() == InventoryReservation.Status.RESERVED)
                .flatMap(res -> {
                    res.setStatus(InventoryReservation.Status.RELEASED);
                    return reservationRepo.save(res)
                            .then(Flux.fromIterable(res.getItems()).concatMap(item -> {
                                Query query = new Query(Criteria.where("id").is(item.getIngredientId()));
                                Update update = new Update().inc("stockOnHand", item.getQuantity());
                                return mongoTemplate.updateFirst(query, update, Ingredient.class);
                            }).then());
                });
    }
}