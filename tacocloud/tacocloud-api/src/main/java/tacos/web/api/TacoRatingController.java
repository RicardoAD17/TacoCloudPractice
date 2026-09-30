package tacos.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.TacoRating;
import tacos.data.TacoRatingRepository;
import tacos.data.TacoRepository;
import tacos.service.TacoRatingAggregationService;
import tacos.web.DTO.RatingRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/tacos", produces = "application/json")
@CrossOrigin(origins="http://localhost:8080")
public class TacoRatingController {

    private final TacoRatingRepository ratingRepo;
    private final TacoRatingAggregationService aggregationService;
    private final TacoRepository tacoRepo;

    public TacoRatingController(TacoRatingRepository ratingRepo, 
                                TacoRatingAggregationService aggregationService, 
                                TacoRepository tacoRepo) {
        this.ratingRepo = ratingRepo;
        this.aggregationService = aggregationService;
        this.tacoRepo = tacoRepo;
    }

    @PutMapping(path = "/{id}/rating", consumes = "application/json")
    public Mono<ResponseEntity<TacoRating>> rateTaco(
            @PathVariable("id") String tacoId, 
            @Valid @RequestBody RatingRequest request) { 
        
        String userId = "usuario-fijo-123"; 
        
        return tacoRepo.findById(tacoId)
            .flatMap(taco -> ratingRepo.findByUserIdAndTacoId(userId, tacoId)
                .defaultIfEmpty(new TacoRating())
                .flatMap(rating -> {
                    rating.setUserId(userId);
                    rating.setTacoId(tacoId);
                    rating.setScore(request.getScore());
                    return ratingRepo.save(rating); 
                })
            )
            .map(saved -> ResponseEntity.ok(saved))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/top")
    public Flux<TacoRatingAggregationService.TopTacoResult> getTopTacos(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "3") int minVotes) {
        
        return aggregationService.getTopTacos(limit, minVotes);
    }
}