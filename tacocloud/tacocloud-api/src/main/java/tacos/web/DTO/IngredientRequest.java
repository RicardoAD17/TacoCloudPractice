package tacos.web.DTO;

import lombok.Getter;
import lombok.Setter;
import tacos.Ingredient.Type;
@Getter
@Setter
public class IngredientRequest {
    private String id;
    private String name;
    private Type type;
}
