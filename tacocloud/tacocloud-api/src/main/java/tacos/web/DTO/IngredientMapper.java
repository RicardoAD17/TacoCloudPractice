package tacos.web.DTO;

import org.springframework.stereotype.Component;
import tacos.Ingredient;

@Component
public class IngredientMapper {

public Ingredient toDomain(IngredientRequest request) {
        return new Ingredient(
            request.getId(), 
            request.getName(), 
            request.getType(), 
            java.math.BigDecimal.ZERO, 
            true, 
            0, 
            10, 
            null
        );
    }

    public IngredientResponse toResponse(Ingredient entity) {
        IngredientResponse response = new IngredientResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setType(entity.getType());
        return response;
    }
}