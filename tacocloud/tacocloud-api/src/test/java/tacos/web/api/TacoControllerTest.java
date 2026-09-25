package tacos.web.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.Ingredient;
import tacos.Ingredient.Type;
import tacos.Taco;
import tacos.data.IngredientRepository;
import tacos.data.TacoRepository;
import tacos.service.TacoValidationService;

public class TacoControllerTest {
  @Autowired
    private WebTestClient webTestClient;
  @MockBean
    private TacoValidationService validationService;
  @Test
    public void shouldReturnRecentTacos() {
        tacos.data.TacoRepository mockRepo = org.mockito.Mockito.mock(tacos.data.TacoRepository.class);
        TacoClassificationService mockClassService = org.mockito.Mockito.mock(TacoClassificationService.class);
        TacoValidationService mockValService = org.mockito.Mockito.mock(TacoValidationService.class);
        Taco tacoPrueba = new Taco();
        tacoPrueba.setName("Taco Reciente");
        org.mockito.Mockito.when(mockRepo.searchTacos(
            org.mockito.ArgumentMatchers.any(), 
            org.mockito.ArgumentMatchers.any(), 
            org.mockito.ArgumentMatchers.any(), 
            org.mockito.ArgumentMatchers.any(), 
            org.mockito.ArgumentMatchers.any(), 
            org.mockito.ArgumentMatchers.any()
        )).thenReturn(reactor.core.publisher.Flux.just(tacoPrueba));
        TacoController tacoController = new TacoController(mockRepo, mockClassService, mockValService,null,null);
        org.springframework.test.web.reactive.server.WebTestClient webTestClient = 
            org.springframework.test.web.reactive.server.WebTestClient.bindToController(tacoController).build();
        webTestClient.get()
            .uri("/api/tacos?recent")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray();
    }

  @Test
  public void shouldSaveATaco() {
    TacoRepository tacoRepo = Mockito.mock(
                TacoRepository.class);
    Mono<Taco> unsavedTacoMono = Mono.just(testTaco(null));
    Taco savedTaco = testTaco(null);
    Mono<Taco> savedTacoMono = Mono.just(savedTaco);

    when(tacoRepo.save(any())).thenReturn(savedTacoMono);

    WebTestClient testClient = WebTestClient.bindToController(
        new TacoController(tacoRepo,null,null,null,null)).build();

    testClient.post()
        .uri("/api/tacos")
        .contentType(MediaType.APPLICATION_JSON)
        .body(unsavedTacoMono, Taco.class)
      .exchange()
      .expectStatus().isCreated()
      .expectBody(Taco.class)
        .isEqualTo(savedTaco);
  }

  private Taco testTaco(Long number) {
    Taco taco = new Taco();
    taco.setId(number != null ? number.toString(): "TESTID");
    taco.setName("Taco " + number);
    List<Ingredient> ingredients = new ArrayList<>();
    Ingredient ingA = new Ingredient();
    ingA.setId("INGA");
    ingA.setName("Ingredient A");
    ingA.setType(Type.WRAP);
    
    Ingredient ingB = new Ingredient();
    ingB.setId("INGB");
    ingB.setName("Ingredient B");
    ingB.setType(Type.PROTEIN);
    
    ingredients.add(ingA);
    ingredients.add(ingB);
    
    taco.setIngredients(ingredients);
    return taco;
  }
 @Test
    public void validateTaco_Returns422_WhenTacoIsInvalid() {
        tacos.data.TacoRepository mockRepo = org.mockito.Mockito.mock(tacos.data.TacoRepository.class);
        TacoClassificationService mockClassService = org.mockito.Mockito.mock(TacoClassificationService.class);
        TacoValidationService mockValService = org.mockito.Mockito.mock(TacoValidationService.class);

        TacoController tacoController = new TacoController(mockRepo, mockClassService, mockValService, null,null);
        org.springframework.test.web.reactive.server.WebTestClient webTestClient = 
            org.springframework.test.web.reactive.server.WebTestClient.bindToController(tacoController).build();
        Taco invalidTaco = new Taco();
        invalidTaco.setName("Taco Malo");
        when(mockValService.validate(any(Taco.class)))
            .thenReturn(Collections.singletonList("MIN_INGREDIENTS_NOT_MET"));
        webTestClient.post()
            .uri("/api/tacos/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidTaco)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.violations").isArray()
            .jsonPath("$.violations[0]").isEqualTo("MIN_INGREDIENTS_NOT_MET");
    }
    @Test
    public void addFavorite_Returns200_WhenTacoExists() {
        tacos.data.TacoRepository mockTacoRepo = org.mockito.Mockito.mock(tacos.data.TacoRepository.class);
        tacos.data.UserRepository mockUserRepo = org.mockito.Mockito.mock(tacos.data.UserRepository.class);
        TacoClassificationService mockClassService = org.mockito.Mockito.mock(TacoClassificationService.class);
        TacoValidationService mockValService = org.mockito.Mockito.mock(TacoValidationService.class);
        tacos.service.TacoOfTheDayService mockTotdService = org.mockito.Mockito.mock(tacos.service.TacoOfTheDayService.class);
        Taco taco = new Taco();
        taco.setId("taco-1");
        
        tacos.User user = new tacos.User();
        user.setId("usuario-fijo-123");
        user.setFavorites(new java.util.ArrayList<>());

        org.mockito.Mockito.when(mockTacoRepo.findById("taco-1"))
            .thenReturn(reactor.core.publisher.Mono.just(taco));
        org.mockito.Mockito.when(mockUserRepo.findById("usuario-fijo-123"))
            .thenReturn(reactor.core.publisher.Mono.just(user));
        org.mockito.Mockito.when(mockUserRepo.save(org.mockito.ArgumentMatchers.any(tacos.User.class)))
            .thenReturn(reactor.core.publisher.Mono.just(user));

        TacoController controller = new TacoController(mockTacoRepo, mockClassService, mockValService, mockTotdService, mockUserRepo);
        org.springframework.test.web.reactive.server.WebTestClient client = 
            org.springframework.test.web.reactive.server.WebTestClient.bindToController(controller).build();
        client.post()
            .uri("/api/tacos/taco-1/favorite")
            .exchange()
            .expectStatus().isOk();
    }
}