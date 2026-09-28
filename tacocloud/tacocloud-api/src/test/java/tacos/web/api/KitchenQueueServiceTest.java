package tacos.web.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.TacoOrder;
import tacos.User;
import tacos.model.OrderStatus;
import tacos.service.KitchenQueueService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class KitchenQueueServiceTest {

    @Test
    public void claimNextOrder_UsesFindAndModifyForAtomicity() {

        ReactiveMongoTemplate mockTemplate = Mockito.mock(ReactiveMongoTemplate.class);
        KitchenQueueService service = new KitchenQueueService(mockTemplate);

        User cook = new User(); cook.setId("cook-1"); cook.setUsername("Cocinero");

        TacoOrder claimedOrder = new TacoOrder();
        claimedOrder.setId("order-999");
        claimedOrder.setStatus(OrderStatus.ACCEPTED);
        Mockito.when(mockTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(TacoOrder.class)))
                .thenReturn(Mono.just(claimedOrder));

        Mono<TacoOrder> result = service.claimNextOrder(cook);

        StepVerifier.create(result)
                .expectNextMatches(order -> order.getId().equals("order-999") && order.getStatus() == OrderStatus.ACCEPTED)
                .verifyComplete();
        
        Mockito.verify(mockTemplate, Mockito.times(1))
                .findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(TacoOrder.class));
    }
}