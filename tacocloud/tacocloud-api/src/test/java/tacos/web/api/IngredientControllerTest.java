package tacos.web.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.Ingredient;
import tacos.data.IngredientRepository;
import tacos.Ingredient.Type;
public class IngredientControllerTest {
    private IngredientRepository repo;
    private WebTestClient client;
    @BeforeEach
    public void setup(){
      repo= Mockito.mock(IngredientRepository.class);
      client= WebTestClient.bindToController(new IngredientController(repo))
      .configureClient()
      .baseUrl("/api/ingredients")
      .build();
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

  //PostMan 
  @Test
  public void testPostIngredients_exito(){
    Ingredient newIngredient= new Ingredient("TOTA", "Tortilla Mini ",Type.WRAP);
    Mockito.when(repo.findById("TOTA")).thenReturn(Mono.empty());
    Mockito.when(repo.save(newIngredient)).thenReturn(Mono.just(newIngredient));
    client.post().contentType(MediaType.APPLICATION_JSON)
    .bodyValue(newIngredient).exchange().expectStatus().isCreated()
    .expectBody(Ingredient.class).isEqualTo(newIngredient);

  }
  @Test
  public void testPostIngredients_error(){
    Ingredient newIngredient= new Ingredient("TOTA", "Tortilla Mini ",null);
    client.post().contentType(MediaType.APPLICATION_JSON)
    .bodyValue(newIngredient).exchange().expectStatus().isBadRequest()
    .expectBody().isEmpty();

  }
}
