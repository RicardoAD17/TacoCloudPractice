package tacos.web.DTO;

import org.springframework.stereotype.Component;

import tacos.TacoOrder;
@Component
public class OrderMapper {
    public TacoOrder toDomain(OrderTacoRequest request){
        TacoOrder order= new TacoOrder();
        order.setDeliveryName(request.getDeliveryName());
        order.setDeliveryCity(request.getDeliveryCity());
        order.setDeliveryState(request.getDeliveryState());
        order.setDeliveryStreet(request.getDeliveryStreet());
        order.setDeliveryZip(request.getDeliveryZip());
        order.setPaymentToken(request.getPaymentToken());
        return order;
    }
    public OrderResponse toResponse(TacoOrder order){
        OrderResponse response= new OrderResponse();
        response.setId(order.getId());
        response.setDeliveryName(order.getDeliveryName());
        if (order.getUser() != null) {
            response.setUsername(order.getUser().getUsername()); 
        }
        return response;
    }
}
