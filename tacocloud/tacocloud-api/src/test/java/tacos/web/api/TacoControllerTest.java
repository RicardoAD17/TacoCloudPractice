package tacos.web.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

public class TacoControllerTest {
  @Test
  public void shouldReturnRecentTacos() {
    Taco[] tacos = {
        testTaco(1L), testTaco(2L),
        testTaco(3L), testTaco(4L),
        testTaco(5L), testTaco(6L),
        testTaco(7L), testTaco(8L),
        testTaco(9L), testTaco(10L),
        testTaco(11L), testTaco(12L),
        testTaco(13L), testTaco(14L),
        testTaco(15L), testTaco(16L)};
    Flux<Taco> tacoFlux = Flux.just(tacos);

    TacoRepository tacoRepo = Mockito.mock(TacoRepository.class);
    when(tacoRepo.findAll()).thenReturn(tacoFlux);

    WebTestClient testClient = WebTestClient.bindToController(
        new TacoController(tacoRepo))
        .build();

    testClient.get().uri("/api/tacos?recent")
      .exchange()
      .expectStatus().isOk()
      .expectBody()
        .jsonPath("$").isArray()
        .jsonPath("$").isNotEmpty()
        .jsonPath("$[0].id").isEqualTo(tacos[0].getId().toString())
        .jsonPath("$[0].name").isEqualTo("Taco 1")
        .jsonPath("$[1].id").isEqualTo(tacos[1].getId().toString())
        .jsonPath("$[1].name").isEqualTo("Taco 2")
        .jsonPath("$[11].id").isEqualTo(tacos[11].getId().toString())
        .jsonPath("$[11].name").isEqualTo("Taco 12")
        .jsonPath("$[12]").doesNotExist();
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
        new TacoController(tacoRepo)).build();

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
    ingredients.add(
        new Ingredient("INGA", "Ingredient A", Type.WRAP));
    ingredients.add(
        new Ingredient("INGB", "Ingredient B", Type.PROTEIN));
    taco.setIngredients(ingredients);
    return taco;
  }
  @Test
  public void testUpdateIngredients_Exito() {
    IngredientRepository repo = Mockito.mock(IngredientRepository.class);
    IngredientController controller = new IngredientController(repo);
    Ingredient ingViejo = new Ingredient("FLTO", "Tortilla Normal", Type.WRAP);
    Ingredient ingNuevo = new Ingredient("FLTO", "Tortilla Gigante", Type.WRAP);
  
    Mockito.when(repo.findById("FLTO")).thenReturn(Mono.just(ingViejo));
    Mockito.when(repo.save(Mockito.any(Ingredient.class))).thenReturn(Mono.just(ingNuevo));
    Mono<ResponseEntity<Ingredient>> resultado = controller.updateIngredient("FLTO", ingNuevo);
    StepVerifier.create(resultado).assertNext(response -> {
      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      Ingredient ingredientResponse = response.getBody();
      assertThat(ingredientResponse).isNotNull();
      assertThat(ingredientResponse.getId()).isEqualTo("FLTO");
      assertThat(ingredientResponse.getName()).isEqualTo("Tortilla Gigante");
    }).verifyComplete();

    Mockito.verify(repo).save(Mockito.any(Ingredient.class));
  }

  @Test
  public void testUpdateIngredients_NoEncontrado() {
    IngredientRepository repo = Mockito.mock(IngredientRepository.class);
    IngredientController controller = new IngredientController(repo);
    Mockito.when(repo.findById("ID_INVALIDO")).thenReturn(Mono.empty());
    Ingredient ingNuevo = new Ingredient("ID_INVALIDO", "Ingrediente Fantasma", Type.WRAP);
    Mono<ResponseEntity<Ingredient>> resultado = controller.updateIngredient("ID_INVALIDO", ingNuevo);
    StepVerifier.create(resultado).assertNext(response -> {
      assertThat(response.getStatusCodeValue()).isEqualTo(404);
      assertThat(response.getBody()).isNull();
    }).verifyComplete();
    Mockito.verify(repo, Mockito.never()).save(Mockito.any(Ingredient.class));
  }
}
