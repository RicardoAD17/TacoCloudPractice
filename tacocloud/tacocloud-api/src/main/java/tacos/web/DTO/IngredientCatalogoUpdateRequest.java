package tacos.web.DTO;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;

import lombok.Getter;
import lombok.Setter;
@Getter 
@Setter 
public class IngredientCatalogoUpdateRequest {
  @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
  private BigDecimal unitPrice;
  private Boolean available;
}
