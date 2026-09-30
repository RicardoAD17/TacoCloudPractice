package tacos;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.Date;

@Data
@Document(collection = "processed_events")
public class ProcessedEvent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;

    private String eventType;
    private String result; 
    private Date processedAt = new Date();

    public ProcessedEvent() {}

    public ProcessedEvent(String eventId, String eventType, String result) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.result = result;
        this.processedAt = new Date();
    }
}