package tacos.web.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull; 
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import tacos.Ingredient.Type;

@Getter
@Setter
public class IngredientRequest {
    @NotBlank(message = "El ID del ingrediente es obligatorio")
    @Size(min = 3, max = 4, message = "El ID debe tener entre 3 y 4 caracteres")
    private String id;
    
    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    private String name;
    
    @NotNull(message = "El tipo de ingrediente es obligatorio") 
    private Type type;
}