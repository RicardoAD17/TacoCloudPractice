package tacos.data; // O el paquete que uses para entidades de BD

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@Document(collection = "idempotency_records")
@CompoundIndex(def = "{'userId': 1, 'idempotencyKey': 1}", unique = true)
public class IdempotencyRecord {

    @Id
    private String id;
    
    private String userId;
    private String idempotencyKey;
    
    private String requestHash;
    private String orderId;   
    private String status;    
    @Indexed(expireAfterSeconds = 86400)
    private Date createdAt = new Date();
}