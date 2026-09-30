package tacos.web.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.web.DTO.OrderDetailDTO;

public class UserOrderControllerTest {

    @Test
    public void myOrderDetails_Returns404_WhenOrderBelongsToSomeoneElse() {
        OrderRepository mockRepo = Mockito.mock(OrderRepository.class);
        UserOrderController controller = new UserOrderController(mockRepo);
        User attackerUser = new User();
        attackerUser.setId("user-A-id");
        Mockito.when(mockRepo.findByIdAndUser_Id("orden-secreta", "user-A-id"))
                .thenReturn(Mono.empty());

        Mono<ResponseEntity<OrderDetailDTO>> responseMono = controller.myOrderDetails("orden-secreta", attackerUser);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getStatusCodeValue() == 404)
                .verifyComplete();
    }
}