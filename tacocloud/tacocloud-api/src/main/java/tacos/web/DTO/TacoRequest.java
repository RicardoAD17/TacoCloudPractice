package tacos.web.DTO;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TacoRequest {
    private String name;
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 50, message = "No puedes ordenar más de 50 tacos de este tipo")
    private int quantity = 1;
    private List<IngredientRequest> ingredients;
}