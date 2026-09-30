package tacos.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tacos.OrderStateTransition;
import tacos.TacoOrder;
import tacos.User;
import tacos.model.OrderStatus;

import java.util.Date;

@Service
public class KitchenQueueService {
    private final ReactiveMongoTemplate mongoTemplate;

    public KitchenQueueService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Flux<TacoOrder> getQueue() {
        Query query = new Query(Criteria.where("status").is(OrderStatus.CREATED))
                .with(Sort.by(Sort.Direction.ASC, "placedAt", "id"));
        return mongoTemplate.find(query, TacoOrder.class);
    }

    public Mono<TacoOrder> claimNextOrder(User cook) {
        Query query = new Query(Criteria.where("status").is(OrderStatus.CREATED))
                .with(Sort.by(Sort.Direction.ASC, "placedAt", "id"));

        OrderStateTransition transition = new OrderStateTransition();
        transition.setFromStatus(OrderStatus.CREATED);
        transition.setToStatus(OrderStatus.ACCEPTED);
        transition.setTimestamp(new Date());
        transition.setActor(cook.getUsername());
        transition.setReason("Reclamado atómicamente por estación de cocina");
        Update update = new Update()
                .set("status", OrderStatus.ACCEPTED)
                .set("cookId", cook.getId())
                .push("statusHistory", transition);

        return mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true), 
                TacoOrder.class
        );
    }

    public Mono<Integer> calculateETA(TacoOrder order) {
        Query queueQuery = new Query(Criteria.where("status").in(OrderStatus.CREATED, OrderStatus.ACCEPTED)
                .and("placedAt").lte(order.getPlacedAt()));
        
        return mongoTemplate.count(queueQuery, TacoOrder.class)
                .map(ordersAhead -> {
                    int baseTime = ordersAhead.intValue() * 5;
                    int tacoTime = order.getTacos() != null ? order.getTacos().size() * 2 : 0;
                    return baseTime + tacoTime;
                });
    }
}