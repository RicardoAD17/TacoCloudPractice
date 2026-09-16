package tacos.web.DTO;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import tacos.Ingredient.Type;
@Getter
@Setter
public class IngredientRequest {
	@NotBlank(message="El ID del ingrediente es obligatorio")
    private String id;
    @NotBlank(message="El nombre del ingrediente es obligatorio")
    private String name;
    @NotBlank(message="El tipo de ingrediente es obligatorio")
    private Type type;
    
}
