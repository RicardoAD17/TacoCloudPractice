package tacos.web.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.Ingredient;
import tacos.data.IngredientRepository;
import tacos.Ingredient.Type;
import tacos.web.DTO.IngredientMapper;
import tacos.web.DTO.IngredientRequest;
import tacos.web.DTO.IngredientResponse;

public class IngredientControllerTest {
    
    private IngredientRepository repo;
    private IngredientMapper mapper;
    private WebTestClient client;
    private IngredientController controller;

    @BeforeEach
    public void setup(){
      repo = Mockito.mock(IngredientRepository.class);
      mapper = new IngredientMapper();
      controller = new IngredientController(repo, mapper);

      client = WebTestClient.bindToController(controller)
      .argumentResolvers(configurer -> configurer.addCustomResolver(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
              return parameter.getParameterType().equals(UriComponentsBuilder.class);
          }
          @Override
          public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
              return Mono.just(UriComponentsBuilder.newInstance().path("http://localhost:8080"));
          }
      }))
      .configureClient()
      .baseUrl("/api/ingredients")
      .build();
    }

    @Test
    public void testUpdateIngredients_Exito() {
        Ingredient ingViejo = new Ingredient("FLTO", "Tortilla Normal", Type.WRAP);
        Ingredient ingGuardado = new Ingredient("FLTO", "Tortilla Gigante", Type.WRAP);

        IngredientRequest reqNuevo = new IngredientRequest();
        reqNuevo.setId("FLTO");
        reqNuevo.setName("Tortilla Gigante");
        reqNuevo.setType(Type.WRAP);

        Mockito.when(repo.findById("FLTO")).thenReturn(Mono.just(ingViejo));
        Mockito.when(repo.save(Mockito.any(Ingredient.class))).thenReturn(Mono.just(ingGuardado));
        
        Mono<ResponseEntity<IngredientResponse>> resultado = controller.updateIngredient("FLTO", reqNuevo);
        
        StepVerifier.create(resultado).assertNext(response -> {
            assertThat(response.getStatusCodeValue()).isEqualTo(200);
            IngredientResponse ingredientResponse = response.getBody();
            assertThat(ingredientResponse).isNotNull();
            assertThat(ingredientResponse.getId()).isEqualTo("FLTO");
            assertThat(ingredientResponse.getName()).isEqualTo("Tortilla Gigante");
        }).verifyComplete();

        Mockito.verify(repo).save(Mockito.any(Ingredient.class));
    }

    @Test
    public void testUpdateIngredients_NoEncontrado() {
        Mockito.when(repo.findById("ID_INVALIDO")).thenReturn(Mono.empty());
        
        IngredientRequest reqNuevo = new IngredientRequest();
        reqNuevo.setId("ID_INVALIDO");
        reqNuevo.setName("Ingrediente Fantasma");
        reqNuevo.setType(Type.WRAP);
        
        Mono<ResponseEntity<IngredientResponse>> resultado = controller.updateIngredient("ID_INVALIDO", reqNuevo);
        
        StepVerifier.create(resultado).assertNext(response -> {
            assertThat(response.getStatusCodeValue()).isEqualTo(404);
            assertThat(response.getBody()).isNull();
        }).verifyComplete();
        
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(Ingredient.class));
    }

    @Test
    public void testPostIngredients_exito(){
        Ingredient newIngredient = new Ingredient("TOTA", "Tortilla Mini", Type.WRAP);
        
        IngredientRequest reqNuevo = new IngredientRequest();
        reqNuevo.setId("TOTA");
        reqNuevo.setName("Tortilla Mini");
        reqNuevo.setType(Type.WRAP);

        Mockito.when(repo.findById("TOTA")).thenReturn(Mono.empty());
        Mockito.when(repo.save(Mockito.any(Ingredient.class))).thenReturn(Mono.just(newIngredient));
        
        client.post().contentType(MediaType.APPLICATION_JSON)
        .bodyValue(reqNuevo).exchange().expectStatus().isCreated()
        .expectBody(IngredientResponse.class)
        .value(response -> {
            assertThat(response.getId()).isEqualTo("TOTA");
            assertThat(response.getName()).isEqualTo("Tortilla Mini");
        });
    }

    @Test
    public void testPostIngredients_error(){
        IngredientRequest reqNuevo = new IngredientRequest();
        reqNuevo.setId("TOTA");
        reqNuevo.setName("Tortilla Mini");
        reqNuevo.setType(null);

        client.post().contentType(MediaType.APPLICATION_JSON)
        .bodyValue(reqNuevo).exchange().expectStatus().isBadRequest()
        .expectBody().isEmpty();
    }
}