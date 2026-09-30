package tacos;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;

import lombok.Data;
import tacos.model.Allergen;
import tacos.model.DietaryTag;
import tacos.model.SpiceLevel;

@Data
@RestResource(rel = "tacos", path = "tacos")
@Document
@CompoundIndex(name = "taco_createdAt_id", def = "{'createdAt': -1, '_id': 1}")
@CompoundIndex(name = "taco_ingredient_ids", def = "{'ingredients._id': 1}")
public class Taco {

  @Id
  private String id;
  
  @NotNull
  @Size(min = 5, message = "Name must be at least 5 characters long")
  @Indexed
  private String name;
  
  @Indexed
  private Date createdAt = new Date();
  
  @Size(min=1, message="You must choose at least 1 ingredient")
  private List<Ingredient> ingredients;

  private java.math.BigDecimal price= java.math.BigDecimal.ZERO;
  
  @javax.validation.constraints.Min(value = 1, message = "La cantidad debe ser al menos 1")
  private int quantity = 1;
  private String discountCode;
  private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
    @Indexed
    private Set<DietaryTag> dietaryTags = new HashSet<>();
    private Set<Allergen> allergens = new HashSet<>();
    @Indexed
    private SpiceLevel spiceLevel = SpiceLevel.NONE;
}
