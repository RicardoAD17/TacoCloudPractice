package tacos.web.DTO;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {
  @NotNull(message = "La cantidad de ajuste es obligatoria")
  private Integer adjustmentQuantity; // Puede ser positivo (entrada) o negativo (salida/merma)
  private String reason;
}