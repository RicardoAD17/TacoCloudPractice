package tacos.web.DTO;

import org.springframework.stereotype.Component;
import tacos.Ingredient;

@Component
public class IngredientMapper {

    public Ingredient toDomain(IngredientRequest request) {
       
        Ingredient ingredient = new Ingredient();
    
        ingredient.setId(request.getId());
        ingredient.setName(request.getName());
        ingredient.setType(request.getType());
        ingredient.setUnitPrice(java.math.BigDecimal.ZERO);
        ingredient.setAvailable(true);
        ingredient.setStockOnHand(0);
        ingredient.setReorderLevel(10);
        
        return ingredient;
    }

    public IngredientResponse toResponse(Ingredient entity) {
        IngredientResponse response = new IngredientResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setType(entity.getType());
        return response;
    }
}