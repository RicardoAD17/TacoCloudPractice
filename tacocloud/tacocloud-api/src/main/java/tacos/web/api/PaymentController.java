package tacos.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import tacos.web.DTO.PaymentGateway;
import tacos.web.DTO.TokenRequest;
import tacos.web.DTO.TokenResponse;
@RestController
@RequestMapping(path = "/api/payment-methods", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {
    private final PaymentGateway paymentGateway;
public PaymentController(PaymentGateway paymentGateway){
        this.paymentGateway = paymentGateway;
    }
    
    @PostMapping(path = "/tokenize", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TokenResponse> tokenizePayment(@RequestBody TokenRequest tokenRequest) {
        return paymentGateway.tokenize(tokenRequest);
    }
}
