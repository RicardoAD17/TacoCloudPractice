package tacos.web.DTO;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull(message = "El score es obligatorio")
    @Min(value = 1, message = "El score mínimo es 1")
    @Max(value = 5, message = "El score máximo es 5")
    private Integer score;
}