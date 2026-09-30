package tacos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "outbox_events")
public class OutboxEvent {
    @Id
    private String eventId; 
    private String payload; 
    private String version; 
    private String status; 
    private int attempts;
    private Date createdAt;
    private Date updatedAt;
}