package tacos;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@Document
public class Ingredient {

  @Id
  private String id;
  private String name;
  private Type type;
  @NotNull(message = "El precio unitario es obligatorio")
  @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
  private BigDecimal unitPrice;
  private boolean available = true;

  @Min(value=0,message = "El stock disponible no puede ser negativo")
  private int stockOnHand;
  @Min(value = 0,message = "El nivel de reorden no puede ser negativo")
  private int reorderLevel;
  @Version 
  private Long version;
  public enum Type {
    WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
  }
  public Ingredient(String id, String name, Type type,BigDecimal unitPrice,int stockOnHand){
    this.id = id;
    this.name = name;
    this.type = type;
    this.unitPrice = unitPrice;
    this.stockOnHand = stockOnHand;
    this.available = stockOnHand > 0;
    this.reorderLevel = 10;
  }

}
