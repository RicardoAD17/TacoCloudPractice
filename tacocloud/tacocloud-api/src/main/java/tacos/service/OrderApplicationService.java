package tacos.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.Taco;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.IngredientRepository;
import tacos.data.OrderRepository;
import tacos.web.DTO.ReorderRequest;
import tacos.web.DTO.ReorderResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepo;
    private final IngredientRepository ingredientRepo; 

    public OrderApplicationService(OrderRepository orderRepo, IngredientRepository ingredientRepo) {
        this.orderRepo = orderRepo;
        this.ingredientRepo = ingredientRepo;
    }

    public Mono<ReorderResponse> reorder(String historicalOrderId, User user, ReorderRequest request) {

        return orderRepo.findByIdAndUser_Id(historicalOrderId, user.getId())
            .flatMap(oldOrder -> {
                
               
                List<String> ingredientIds = oldOrder.getTacos().stream()
                        .flatMap(taco -> taco.getIngredients().stream())
                        .map(Ingredient::getId)
                        .distinct()
                        .collect(Collectors.toList());

               
                return ingredientRepo.findAllById(ingredientIds)
                        .collectMap(Ingredient::getId) 
                        .flatMap(currentIngredientsMap -> {
                            
                            ReorderResponse response = new ReorderResponse();
                            List<String> warnings = new ArrayList<>();
                            List<String> outOfStockNames = new ArrayList<>();

                            double oldPrice = 0.0;
                            double currentPrice = 0.0;
                            boolean isOutOfStock = false;

                            List<Taco> newTacos = new ArrayList<>();

                            for (Taco oldTaco : oldOrder.getTacos()) {
                                Taco newTaco = new Taco();
                                newTaco.setName(oldTaco.getName());
                                List<Ingredient> freshIngredients = new ArrayList<>();

                                for (Ingredient oldIng : oldTaco.getIngredients()) {
                                    oldPrice += oldIng.getUnitPrice() != null ? oldIng.getUnitPrice().doubleValue() : 0.0;
                                    Ingredient currentIng = currentIngredientsMap.get(oldIng.getId());
                                    if (currentIng == null || currentIng.getStockOnHand() <= 0) {
                                        isOutOfStock = true;
                                        outOfStockNames.add(oldIng.getName()); 
                                    } else {
                                       
                                        currentPrice += currentIng.getUnitPrice().doubleValue();
                                        freshIngredients.add(currentIng); 
                                    }
                                }
                                newTaco.setIngredients(freshIngredients);
                                newTacos.add(newTaco);
                            }

                            response.setOldTotal(oldPrice);
                            response.setNewTotal(currentPrice);

                            if (isOutOfStock) {
                                response.setStatus("CONFLICT");
                                warnings.add("Los siguientes ingredientes están agotados: " + String.join(", ", outOfStockNames));
                                response.setWarnings(warnings);
                                return Mono.just(response);
                            }

                        
                            if (currentPrice > oldPrice && !request.isConfirmPriceChange()) {
                                response.setStatus("QUOTE");
                                warnings.add("El precio ha subido de $" + oldPrice + " a $" + currentPrice + ". Por favor confirma el nuevo total con confirmPriceChange: true.");
                                response.setWarnings(warnings);
                                return Mono.just(response);
                            }

                            TacoOrder newOrder = new TacoOrder();
                            newOrder.setUser(user);
                            newOrder.setPlacedAt(new Date()); 
                            newOrder.setDeliveryName(oldOrder.getDeliveryName());
                            newOrder.setDeliveryStreet(oldOrder.getDeliveryStreet());
                            newOrder.setDeliveryCity(oldOrder.getDeliveryCity());
                            newOrder.setDeliveryState(oldOrder.getDeliveryState());
                            newOrder.setDeliveryZip(oldOrder.getDeliveryZip());
                            newOrder.setTacos(newTacos); 
                            
                            return orderRepo.save(newOrder).map(savedOrder -> {
                                response.setStatus("CONFIRMED");
                                response.setNewOrderId(savedOrder.getId());
                                response.setWarnings(warnings);
                                return response;
                            });
                        });
            });
    }
}