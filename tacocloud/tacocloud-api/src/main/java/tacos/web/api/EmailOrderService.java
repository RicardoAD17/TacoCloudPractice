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
    // TODO: Probably should handle unhappy case where email address doesn't match a given user or
    //       where the user doesn't have at least one payment method.
    return emailOrder.flatMap(eOrder->{
      return userRepo.findByEmail(eOrder.getEmail()).switchIfEmpty( Mono.error(new IllegalArgumentException("Usuario no Encontrado")))
        .flatMap(user->{
          return paymentMethodRepo.findByUserId(user.getId()).switchIfEmpty(Mono.error(new IllegalArgumentException("Método de pago no encontrado")))
          .flatMap(payMethod->{
            TacoOrder order= new TacoOrder();
            order.setUser(user);
            order.setCcNumber(payMethod.getCcNumber());
            order.setCcCVV(payMethod.getCcCVV());
            order.setCcExpiration(payMethod.getCcExpiration());
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
    /*return emailOrder.flatMap(eOrder -> {
      Mono<User> userMono = userRepo.findByEmail(eOrder.getEmail());
      Mono<PaymentMethod> paymentMono = userMono.flatMap(user -> {
        return paymentMethodRepo.findByUserId(user.getId());
      });
      return Mono.zip(userMono, paymentMono)
          .flatMap(tuple -> {
            User user = tuple.getT1();
            PaymentMethod paymentMethod = tuple.getT2();
            TacoOrder order = new TacoOrder();
            order.setUser(user);
            order.setCcNumber(paymentMethod.getCcNumber());
            order.setCcCVV(paymentMethod.getCcCVV());
            order.setCcExpiration(paymentMethod.getCcExpiration());
            order.setDeliveryName(user.getFullname());
            order.setDeliveryStreet(user.getStreet());
            order.setDeliveryCity(user.getCity());
            order.setDeliveryState(user.getState());
            order.setDeliveryZip(user.getZip());
            order.setPlacedAt(new Date());

            return emailOrder.map(eOrd -> {
              List<EmailTaco> emailTacos = eOrd.getTacos();
              for (EmailTaco emailTaco : emailTacos) {
                List<String> ingredientIds = emailTaco.getIngredients();
                List<Ingredient> ingredients = new ArrayList<>();
                for (String ingredientId : ingredientIds) {
                  Mono<Ingredient> ingredientMono = ingredientRepo.findById(ingredientId);
                  ingredientMono.subscribe(ingredient ->
                      ingredients.add(ingredient));
                }
                Taco taco = new Taco();
                taco.setName(emailTaco.getName());
                taco.setIngredients(ingredients);
                order.addTaco(taco);
              }
              return order;
            });
          });
    });*/
  }

}
