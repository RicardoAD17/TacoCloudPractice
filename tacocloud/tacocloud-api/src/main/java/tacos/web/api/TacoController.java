package tacos.web.api;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Taco;
import tacos.data.TacoRepository;
import tacos.service.TacoValidationService;

@RestController
@RequestMapping(path = "/api/tacos", produces = "application/json")
@CrossOrigin(origins="http://localhost:8080")
public class TacoController {
  private TacoRepository tacoRepo;
  private TacoClassificationService tacoClassificationService;
  private final TacoValidationService validationService;
  public TacoController(TacoRepository tacoRepo,TacoClassificationService tacoClassificationService,TacoValidationService validationService) {
    this.tacoRepo = tacoRepo;
    this.tacoClassificationService = tacoClassificationService;
    this.validationService= validationService;
  }

@GetMapping
  public Flux<Taco> getTacos(
          @RequestParam(required = false) String name,
          @RequestParam(required = false) String ingredientId,
          @RequestParam(required = false) String diet,
          @RequestParam(required = false) String excludeAllergen,
          @RequestParam(required = false) String spice,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size,
          @RequestParam(defaultValue = "createdAt") String sortStr,
          @RequestParam(defaultValue = "desc") String direction,
          @RequestParam(required = false) String recent) {

     
      int safeSize = Math.min(size, 50);
      safeSize = Math.max(safeSize, 1);

     
      List<String> allowedSortFields = java.util.Arrays.asList("createdAt", "name", "price");String safeSort = allowedSortFields.contains(sortStr) ? sortStr : "createdAt";
      Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
      if (recent != null) {
          page = 0;
          safeSize = 12;
          safeSort = "createdAt";
          sortDirection = Sort.Direction.DESC;
      }

      PageRequest pageRequest = PageRequest.of(page, safeSize, Sort.by(sortDirection, safeSort));

      return tacoRepo.searchTacos(name, ingredientId, diet, excludeAllergen, spice, pageRequest)
                     .doOnNext(tacoClassificationService::classifyTaco);
  }

  @PostMapping(consumes = "application/json")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Taco> postTaco(@RequestBody Taco taco) {
    return tacoRepo.save(taco);
  }

  @GetMapping("/{id}")
  public Mono<Taco> tacoById(@PathVariable("id") String id) {
    return tacoRepo.findById(id);
  }
  @PostMapping(path = "/validate", consumes = "application/json")
  public Mono<ResponseEntity<Object>> validateTacoDesign(@RequestBody Taco taco) {
      List<String> violations = validationService.validate(taco);
      
      if (violations.isEmpty()) {
          return Mono.just(ResponseEntity.ok().build());
      } else {
          return Mono.just(ResponseEntity.unprocessableEntity()
                  .body(Collections.singletonMap("violations", violations)));
      }
  }
}
