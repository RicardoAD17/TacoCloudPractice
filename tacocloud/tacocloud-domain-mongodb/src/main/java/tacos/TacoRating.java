package tacos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document
@CompoundIndex(def = "{'userId': 1, 'tacoId': 1}", unique = true)
public class TacoRating {
    @Id
    private String id;
    private String userId;
    private String tacoId;
    private Integer score;
}