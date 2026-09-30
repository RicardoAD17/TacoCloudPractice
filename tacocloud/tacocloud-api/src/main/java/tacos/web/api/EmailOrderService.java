package tacos.web.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.TacoOrder;
import tacos.PaymentMethod;
import tacos.Taco;
import tacos.User;
import tacos.data.IngredientRepository;
import tacos.data.PaymentMethodRepository;
import tacos.data.UserRepository;
import tacos.web.api.EmailOrder.EmailTaco;

@Service
public class EmailOrderService {

  private UserRepository userRepo;
  private IngredientRepository ingredientRepo;
  private PaymentMethodRepository paymentMethodRepo;

  public EmailOrderService(UserRepository userRepo, IngredientRepository ingredientRepo,
      PaymentMethodRepository paymentMethodRepo) {
    this.userRepo = userRepo;
    this.ingredientRepo = ingredientRepo;
    this.paymentMethodRepo = paymentMethodRepo;
  }

  public Mono<TacoOrder> convertEmailOrderToDomainOrder(Mono<EmailOrder> emailOrder) {
    //       where the user doesn't have at least one payment method.
    return emailOrder.flatMap(eOrder->{
      return userRepo.findByEmail(eOrder.getEmail()).switchIfEmpty( Mono.error(new IllegalArgumentException("Usuario no Encontrado")))
        .flatMap(user->{
          return paymentMethodRepo.findByUserId(user.getId()).switchIfEmpty(Mono.error(new IllegalArgumentException("Método de pago no encontrado")))
          .flatMap(payMethod->{
            TacoOrder order= new TacoOrder();
            order.setUser(user);
            order.setPaymentToken(payMethod.getPaymentToken());
            order.setDeliveryName(user.getFullname());
            order.setDeliveryStreet(user.getStreet());
            order.setDeliveryState(user.getState());
            order.setDeliveryCity(user.getCity());
            order.setDeliveryZip(user.getZip());
            order.setPlacedAt(new Date());
            List<EmailTaco> emailTacos= eOrder.getTacos();
            return Flux.fromIterable(emailTacos)
            .concatMap(emailTaco->{
              Taco taco= new Taco();
              taco.setName(emailTaco.getName());
              return Flux.fromIterable(emailTaco.getIngredients())
              .concatMap(ingId -> ingredientRepo.findById(ingId)
              .switchIfEmpty(Mono.error(new IllegalArgumentException("Ingrediente desconocido:"+ingId)))
            ).collectList().map(ingredientesDeBd->{
              taco.setIngredients(ingredientesDeBd);
              return taco;
            });
            }).collectList().map(tacosCompletos ->{
              tacosCompletos.forEach(order::addTaco);
              return order;
            });
          });      
      });
      });
  }

}
