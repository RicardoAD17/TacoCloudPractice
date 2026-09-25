package tacos.data;

import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;



import reactor.core.publisher.Flux;
import tacos.Taco;

public class TacoRepositoryCustomImpl implements TacoRepositoryCustom {
    private final ReactiveMongoTemplate mongoTemplate;

    public TacoRepositoryCustomImpl(ReactiveMongoTemplate mongoTemplate){
        this.mongoTemplate= mongoTemplate;
    }
    @Override 
    public Flux<Taco> searchTacos(String name, String ingredientId,String diet,String excludeAllergen,String spice,Pageable pageable){
        Query query= new Query();
        if(name != null && !name.trim().isEmpty()){
            String escapedName = Pattern.quote(name.trim());
            query.addCriteria(Criteria.where("name").regex(".*"+escapedName+".*","i"));
        }
        if (ingredientId != null && !ingredientId.trim().isEmpty()) {
            query.addCriteria(Criteria.where("ingredients.id").is(ingredientId));
        }
        if (diet != null && !diet.trim().isEmpty()) {
            query.addCriteria(Criteria.where("dietaryTags").in(diet));
        }
        if (excludeAllergen != null && !excludeAllergen.trim().isEmpty()) {
            query.addCriteria(Criteria.where("allergens").nin(excludeAllergen));
        }
        if (spice != null && !spice.trim().isEmpty()) {
            query.addCriteria(Criteria.where("spiceLevel").is(spice));
        }  
        query.with(pageable);

        return mongoTemplate.find(query, Taco.class);
    }
}
