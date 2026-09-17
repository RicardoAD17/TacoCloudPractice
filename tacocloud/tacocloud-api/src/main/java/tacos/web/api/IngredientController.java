package tacos.web.api;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.data.IngredientRepository;
import tacos.security.error.ConflictException;
import tacos.security.error.NotFoundException;
import tacos.web.DTO.IngredientMapper;
import tacos.web.DTO.IngredientRequest;
import tacos.web.DTO.IngredientResponse;

@RestController
@RequestMapping(path="/api/ingredients", produces="application/json")
@CrossOrigin(origins="http://localhost:8080")
public class IngredientController {

  private final IngredientRepository repo;
  private final IngredientMapper mapper; 

  public IngredientController(IngredientRepository repo, IngredientMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @GetMapping
  public Flux<IngredientResponse> allIngredients() { 
    return repo.findAll()
               .map(mapper::toResponse); 
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<IngredientResponse>> byId(@PathVariable String id) {
    return repo.findById(id)
               .map(ingredient -> ResponseEntity.ok(mapper.toResponse(ingredient))) 
               .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<IngredientResponse>> updateIngredient(@PathVariable String id, 
                                                                   @RequestBody IngredientRequest request) { 
    if (!request.getId().equals(id)) {
      return Mono.just(ResponseEntity.badRequest().build());
    }
    
    return repo.findById(id).flatMap(existing -> {
      existing.setName(request.getName());
      existing.setType(request.getType());
      return repo.save(existing);
    })
    .map(saved -> ResponseEntity.ok(mapper.toResponse(saved))) 
    .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping(consumes = "application/json")
  public Mono<ResponseEntity<IngredientResponse>> postIngredient(@Valid @RequestBody IngredientRequest request, 
                                                                 UriComponentsBuilder uriBuilder) {
    if (request.getId() == null || request.getId().trim().isEmpty() || 
        request.getName() == null || request.getName().trim().isEmpty() || 
        request.getType() == null) {
        return Mono.just(ResponseEntity.badRequest().build());
    }

    return repo.findById(request.getId())
        .flatMap(existing -> Mono.<ResponseEntity<IngredientResponse>>error(new ConflictException("Ya existe un ingrediente con el ID:"+request.getId())))
        .switchIfEmpty(
            Mono.defer(() -> {
                Ingredient ingredientToSave = mapper.toDomain(request);
                
                return repo.save(ingredientToSave)
                    .map(saved -> {
                        URI location = uriBuilder.path("/{id}").buildAndExpand(saved.getId()).toUri();
                        return ResponseEntity.created(location).body(mapper.toResponse(saved));
                    });
            })
        );
  }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteIngredient(@PathVariable String id) {
      return repo.findById(id).flatMap(existing -> {
        return repo.deleteById(id).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
      })
      .switchIfEmpty(Mono.error(new NotFoundException("No se puede eliminar. No se encontro el ingrediente con ID:"+id))); 
    }
}