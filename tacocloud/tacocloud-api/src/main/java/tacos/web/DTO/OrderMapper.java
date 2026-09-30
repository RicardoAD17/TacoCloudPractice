package tacos.web.DTO;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import tacos.TacoOrder;
import tacos.Taco;
import tacos.Ingredient;

@Component
public class OrderMapper {
    
    public TacoOrder toDomain(OrderTacoRequest request) {
        TacoOrder order = new TacoOrder();
        order.setDeliveryName(request.getDeliveryName());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setDeliveryState(request.getDeliveryState());
        order.setDeliveryStreet(request.getDeliveryStreet());
        order.setDeliveryZip(request.getDeliveryZip());
        order.setPaymentToken(request.getPaymentToken());
        order.setDiscountCode(request.getDiscountCode());
        if (request.getTacos() != null) {
            List<Taco> domTacos = new ArrayList<>();
            for (TacoRequest tr : request.getTacos()) {
                Taco t = new Taco();
                t.setName(tr.getName());
                t.setQuantity(tr.getQuantity() > 0 ? tr.getQuantity() : 1);
                if (tr.getIngredients() != null) {
                    List<Ingredient> domIngs = new ArrayList<>();
                    for (IngredientRequest ir : tr.getIngredients()) {
                        Ingredient ing = new Ingredient();
                        ing.setId(ir.getId());
                        domIngs.add(ing);
                    }
                    t.setIngredients(domIngs);
                }
                domTacos.add(t);
            }
            order.setTacos(domTacos);
        }
        return order;
    }
    
    public OrderResponse toResponse(TacoOrder order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setDeliveryName(order.getDeliveryName());
        if (order.getUser() != null) {
            response.setUsername(order.getUser().getUsername()); 
        }
        response.setSubtotal(order.getSubtotal());
        response.setTotal(order.getTotal());
        response.setDiscountAmount(order.getDiscountAmount()); 
        if (order.getTacos() != null) {
            List<TacoResponse> trList = order.getTacos().stream().map(taco -> {
                TacoResponse tr = new TacoResponse();
                tr.setName(taco.getName());
                tr.setQuantity(taco.getQuantity());
                tr.setPrice(taco.getPrice());
                return tr;
            }).collect(Collectors.toList());
            response.setTacos(trList);
        }
        
        return response;
    }
}