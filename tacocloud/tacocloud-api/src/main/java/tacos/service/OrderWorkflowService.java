package tacos.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tacos.OrderStateTransition;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.model.OrderStatus;

import java.util.ArrayList;
import java.util.Date;

@Service
public class OrderWorkflowService {

    private final OrderRepository orderRepo;

    public OrderWorkflowService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    public Mono<TacoOrder> transitionState(String orderId, OrderStatus targetStatus, User actor, String reason) {
        return orderRepo.findById(orderId)
            .flatMap(order -> {
                OrderStatus current = order.getStatus();

                if (current == targetStatus) {
                    return Mono.just(order);
                }

                if (!isValidTransition(current, targetStatus)) {
                    return Mono.error(new IllegalStateException("Transición inválida de " + current + " a " + targetStatus));
                }
                if (!isAuthorized(order, targetStatus, actor)) {
                    return Mono.error(new SecurityException("No autorizado para cambiar a estado " + targetStatus));
                }

                OrderStateTransition transition = new OrderStateTransition();
                transition.setFromStatus(current);
                transition.setToStatus(targetStatus);
                transition.setTimestamp(new Date());
                transition.setActor(actor.getUsername());
                transition.setReason(reason);

                if (order.getStatusHistory() == null) {
                    order.setStatusHistory(new ArrayList<>());
                }
                order.getStatusHistory().add(transition);

                order.setStatus(targetStatus);
                return orderRepo.save(order);
            });
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus target) {
        switch (current) {
            case CREATED: return target == OrderStatus.ACCEPTED || target == OrderStatus.CANCELLED;
            case ACCEPTED: return target == OrderStatus.PREPARING || target == OrderStatus.CANCELLED;
            case PREPARING: return target == OrderStatus.READY;
            case READY: return target == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY: return target == OrderStatus.DELIVERED;
            default: return false;
        }
    }

    private boolean isAuthorized(TacoOrder order, OrderStatus target, User actor) {
        String role = actor.getRole() != null ? actor.getRole() : "ROLE_USER";
        
        if (target == OrderStatus.CANCELLED) {
            return role.contains("ADMIN") || actor.getId().equals(order.getUser().getId());
        }
        return role.contains("ADMIN") || role.contains("STAFF") || role.contains("KITCHEN");
    }
}