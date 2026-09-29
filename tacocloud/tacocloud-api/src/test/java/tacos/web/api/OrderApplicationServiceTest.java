package tacos.web.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.Ingredient;
import tacos.Taco;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.IngredientRepository;
import tacos.data.OrderRepository;
import tacos.service.OrderApplicationService;
import tacos.web.DTO.ReorderRequest;
import tacos.web.DTO.ReorderResponse;
import static org.mockito.Mockito.mock;
import java.math.BigDecimal;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import tacos.data.OutboxEventRepository;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

public class OrderApplicationServiceTest {

    @Test
    public void reorder_ReturnsQuote_WhenPriceChangedAndNotConfirmed() {
        OrderRepository orderRepo = Mockito.mock(OrderRepository.class);
        IngredientRepository ingredientRepo = Mockito.mock(IngredientRepository.class);
        OutboxEventRepository outboxRepo = Mockito.mock(OutboxEventRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();

        OrderApplicationService service = new OrderApplicationService(orderRepo, ingredientRepo, outboxRepo,objectMapper);
        User user = new User(); user.setId("user-1");
        TacoOrder oldOrder = new TacoOrder(); oldOrder.setId("orden-antigua");
        Ingredient oldIng = new Ingredient("ING-1", "Carnitas", Ingredient.Type.PROTEIN, new BigDecimal("25.00"), 10);
        Taco oldTaco = new Taco(); oldTaco.setName("Taco Original");
        oldTaco.setIngredients(Arrays.asList(oldIng));
        oldOrder.setTacos(Arrays.asList(oldTaco));
        Ingredient currentIng = new Ingredient("ING-1", "Carnitas", Ingredient.Type.PROTEIN, new BigDecimal("28.50"), 100);
        
        Mockito.when(orderRepo.findByIdAndUser_Id("orden-antigua", "user-1")).thenReturn(Mono.just(oldOrder));
        Mockito.when(ingredientRepo.findAllById(anyList())).thenReturn(Flux.just(currentIng));

        ReorderRequest req = new ReorderRequest();
        req.setConfirmPriceChange(false);

        Mono<ReorderResponse> response = service.reorder("orden-antigua", user, req);

        StepVerifier.create(response)
                .expectNextMatches(res -> res.getStatus().equals("QUOTE") && res.getNewTotal() > res.getOldTotal())
                .verifyComplete();
    }
    
    @Test
    public void reorder_CreatesNewOrder_WhenConfirmed() {
        OrderRepository orderRepo = Mockito.mock(OrderRepository.class);
        IngredientRepository ingredientRepo = Mockito.mock(IngredientRepository.class);
        OutboxEventRepository outboxRepo = Mockito.mock(OutboxEventRepository.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OrderApplicationService service = new OrderApplicationService(orderRepo, ingredientRepo, outboxRepo, objectMapper);
        User user = new User(); user.setId("user-1");
        TacoOrder oldOrder = new TacoOrder(); oldOrder.setId("orden-antigua");
        Ingredient oldIng = new Ingredient("ING-1", "Carnitas", Ingredient.Type.PROTEIN, new BigDecimal("25.00"), 10);
        Taco oldTaco = new Taco(); oldTaco.setName("Taco Original");
        oldTaco.setIngredients(Arrays.asList(oldIng));
        oldOrder.setTacos(Arrays.asList(oldTaco));
        
        TacoOrder newSavedOrder = new TacoOrder(); newSavedOrder.setId("orden-nueva-generada");

     
        Ingredient currentIng = new Ingredient("ING-1", "Carnitas", Ingredient.Type.PROTEIN, new BigDecimal("28.50"), 100);
        Mockito.when(orderRepo.findByIdAndUser_Id("orden-antigua", "user-1")).thenReturn(Mono.just(oldOrder));
        Mockito.when(ingredientRepo.findAllById(anyList())).thenReturn(Flux.just(currentIng));
        Mockito.when(orderRepo.save(any(TacoOrder.class))).thenReturn(Mono.just(newSavedOrder));
        tacos.OutboxEvent mockSavedOutbox = new tacos.OutboxEvent();
        mockSavedOutbox.setEventId("dummy-event-id");
        Mockito.when(outboxRepo.save(any(tacos.OutboxEvent.class))).thenReturn(Mono.just(mockSavedOutbox));
        ReorderRequest req = new ReorderRequest();
        req.setConfirmPriceChange(true); 

        Mono<ReorderResponse> response = service.reorder("orden-antigua", user, req);

        StepVerifier.create(response)
                .expectNextMatches(res -> res.getStatus().equals("CONFIRMED") && res.getNewOrderId().equals("orden-nueva-generada"))
                .verifyComplete();
    }
}