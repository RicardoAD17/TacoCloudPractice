package tacos.web.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.model.OrderStatus;
import tacos.service.OrderWorkflowService;

import static org.mockito.ArgumentMatchers.any;

public class OrderWorkflowServiceTest {

    @Test
    public void transitionState_FailsWith409_WhenTransitionIsInvalid() {
        OrderRepository mockRepo = Mockito.mock(OrderRepository.class);
        OrderWorkflowService service = new OrderWorkflowService(mockRepo);

        User admin = new User(); admin.setRole("ROLE_ADMIN");
        TacoOrder order = new TacoOrder(); 
        order.setStatus(OrderStatus.CREATED); 
        order.setUser(new User());

        Mockito.when(mockRepo.findById("order1")).thenReturn(Mono.just(order));

        Mono<TacoOrder> result = service.transitionState("order1", OrderStatus.DELIVERED, admin, "Entrega mágica");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException) 
                .verify();
    }

    @Test
    public void transitionState_Succeeds_WhenTransitionIsValid() {
        OrderRepository mockRepo = Mockito.mock(OrderRepository.class);
        OrderWorkflowService service = new OrderWorkflowService(mockRepo);

        User admin = new User(); admin.setRole("ROLE_ADMIN");
        TacoOrder order = new TacoOrder(); 
        order.setStatus(OrderStatus.CREATED);
        order.setUser(new User());

        Mockito.when(mockRepo.findById("order1")).thenReturn(Mono.just(order));
        Mockito.when(mockRepo.save(any(TacoOrder.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        Mono<TacoOrder> result = service.transitionState("order1", OrderStatus.ACCEPTED, admin, "Orden revisada");

        StepVerifier.create(result)
                .expectNextMatches(saved -> 
                        saved.getStatus() == OrderStatus.ACCEPTED && 
                        saved.getStatusHistory().size() == 1)
                .verifyComplete();
    }
}