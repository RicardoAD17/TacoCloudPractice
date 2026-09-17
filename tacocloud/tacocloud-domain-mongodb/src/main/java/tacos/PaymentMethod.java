package tacos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "payment_methods")
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE) 
public class PaymentMethod {

    @Id
    private String id;
    
   
    private User user;
    
    
    private String paymentToken;
  
    private String brand;
    private String last4;
    private String expiration; 

    public PaymentMethod(User user, String paymentToken, String brand, String last4, String expiration) {
        this.user = user;
        this.paymentToken = paymentToken;
        this.brand = brand;
        this.last4 = last4;
        this.expiration = expiration;
    }
}