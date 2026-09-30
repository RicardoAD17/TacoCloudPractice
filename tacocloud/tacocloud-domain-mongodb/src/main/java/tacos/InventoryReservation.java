package tacos;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document
public class InventoryReservation {
    @Id
    private String id;
    private String orderId;
    private String idempotencyKey;
    private List<ReservedItem> items;
    private Status status = Status.RESERVED;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class ReservedItem {
        private String ingredientId;
        private int quantity;

        public ReservedItem(String ingredientId, int quantity) {
            this.ingredientId = ingredientId;
            this.quantity = quantity;
        }
    }

    public enum Status {
        RESERVED, COMMITTED, RELEASED
    }
}