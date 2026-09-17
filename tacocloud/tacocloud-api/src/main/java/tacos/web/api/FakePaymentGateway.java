package tacos.web.api;

import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import tacos.web.DTO.PaymentGateway;
import tacos.web.DTO.TokenRequest;
import tacos.web.DTO.TokenResponse;

@Service 
@Slf4j 

public class FakePaymentGateway implements PaymentGateway {

    @Override
    public Mono<TokenResponse> tokenize(TokenRequest request) {
        return Mono.fromCallable(()->{
            String last4= "XXXX";
            if (request.getCcNumber()!=null && request.getCcNumber().length()>=4) {
                last4=request.getCcNumber().substring(request.getCcNumber().length()-4);
            }
            String brand = "UNKNOWN";
            if (request.getCcNumber() != null) {
                if (request.getCcNumber().startsWith("4")) brand = "VISA";
                else if (request.getCcNumber().startsWith("5")) brand = "MASTERCARD";
                else if (request.getCcNumber().startsWith("3")) brand = "AMEX";
            }
            TokenResponse response=new TokenResponse();
            response.setPaymentToken(UUID.randomUUID().toString());
            response.setBrand(brand);
            response.setLast4(last4);
            response.setExpiration(request.getCcExpiration());
            return response;
        });
    }

}
