package tacos.web.api;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.data.IngredientRepository;

@RestController
@RequestMapping(path="/api/ingredients", produces="application/json")
@CrossOrigin(origins="http://localhost:8080")
public class IngredientController {

  private IngredientRepository repo;

  public IngredientController(IngredientRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public Flux<Ingredient> allIngredients() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Ingredient>> byId(@PathVariable String id) {
    return repo.findById(id).map(ingredient->ResponseEntity.ok(ingredient)).defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<Ingredient>> updateIngredient(@PathVariable String id,@RequestBody Ingredient ingredient) {
    if (!ingredient.getId().equals(id)) {
      return Mono.just(new ResponseEntity<Ingredient>(HttpStatus.BAD_REQUEST));
    }
     return repo.findById(id).flatMap(existing->{
      existing.setName(ingredient.getName());
      existing.setType(ingredient.getType());
      return repo.save(existing);
     }).map(saved->ResponseEntity.ok(saved)).defaultIfEmpty(ResponseEntity.notFound().build());
  }
  /*
  *HttpHeaders headers = new HttpHeaders();
          headers.setLocation(URI.create("http://localhost:8080/ingredients/" + i.getId()));
  *
  */
  @PostMapping(consumes = "application/json")
  public Mono<ResponseEntity<Ingredient>> postIngredient(@RequestBody Ingredient ingredient,UriComponentsBuilder uriBuilder) {
    if (ingredient.getId() == null || ingredient.getId().trim().isEmpty() || 
          ingredient.getName() == null || ingredient.getName().trim().isEmpty() || 
          ingredient.getType() == null) {
          
          return Mono.just(ResponseEntity.badRequest().build());
    }
    return repo.findById(ingredient.getId())
        //detectar si ya existe
        .map(existing -> ResponseEntity.badRequest().<Ingredient>build())
        .switchIfEmpty(
            Mono.defer(() -> repo.save(ingredient) // MonoDefer
                .map(i -> {
                    URI location = uriBuilder
                            .path("/{id}").buildAndExpand(i.getId()).toUri();
                    return ResponseEntity.created(location).body(i);
                }))
        );
  }

  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteIngredient(@PathVariable String id) {
    //el uso de .flatmap ayuda a los difetenes casos de prueba 
    return repo.findById(id).flatMap(existing -> {
      return repo.deleteById(id).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
    })//el id se encontro y elimino
    .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND)); //el id no se encontro
  }


}
