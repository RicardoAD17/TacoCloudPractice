package tacos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Document
public class TacoOrder implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private String id;
  private Date placedAt = new Date();

  private User user;

  private String deliveryName;

  private String deliveryStreet;

  private String deliveryCity;

  private String deliveryState;

  private String deliveryZip;
  @JsonIgnore
  private String paymentToken;

  private List<Taco> tacos = new ArrayList<>();

  public void addTaco(Taco design) {
    this.tacos.add(design);
  }
  private java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
  
  private java.math.BigDecimal total = java.math.BigDecimal.ZERO;
  private String discountCode;
  private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
}
