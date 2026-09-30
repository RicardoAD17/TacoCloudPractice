package tacos.data;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import tacos.Taco;

public interface TacoRepositoryCustom {
    Flux<Taco> searchTacos(String name, String ingredientId, String diet, 
                           String excludeAllergen, String spice, Pageable pageable);
}