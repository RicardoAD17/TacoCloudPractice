package tacos.web.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tacos.Ingredient;
import tacos.Taco;
import tacos.TacoOrder;
import tacos.data.IngredientRepository;
import tacos.web.DTO.QuoteResponse;
import tacos.pricing.CouponService;
@Service
public class OrderPricingService {

    private final IngredientRepository ingredientRepo;
    private final CouponService couponService;
    public OrderPricingService(IngredientRepository ingredientRepo,CouponService couponService) {
        this.ingredientRepo = ingredientRepo;
        this.couponService= couponService;

    }

    public Mono<TacoOrder> calculateOrderTotals(TacoOrder order) {
        return ingredientRepo.findAll()
            .collectMap(Ingredient::getId)
            .map(ingredientMap -> {
                BigDecimal orderSubtotal = BigDecimal.ZERO;

                if (order.getTacos() != null) {
                    for (Taco taco : order.getTacos()) {
                        BigDecimal tacoBasePrice = BigDecimal.ZERO;

                        if (taco.getIngredients() != null) {
                            for (int i = 0; i < taco.getIngredients().size(); i++) {
                                Ingredient ingRequerido = taco.getIngredients().get(i);
                                Ingredient ingReal = ingredientMap.get(ingRequerido.getId());

                                if (ingReal != null) {
                                    taco.getIngredients().set(i, ingReal); 
                                    
                                    BigDecimal price = ingReal.getUnitPrice() != null ? ingReal.getUnitPrice() : BigDecimal.ZERO;
                                    tacoBasePrice = tacoBasePrice.add(price);
                                }
                            }
                        }
                        
                        tacoBasePrice = tacoBasePrice.setScale(2, RoundingMode.HALF_UP);
                        taco.setPrice(tacoBasePrice);
                        int qty = taco.getQuantity();
                        BigDecimal tacoTotal = tacoBasePrice.multiply(BigDecimal.valueOf(qty))
                                                            .setScale(2, RoundingMode.HALF_UP); 
                        
                        orderSubtotal = orderSubtotal.add(tacoTotal);
                    }
                }
                orderSubtotal = orderSubtotal.setScale(2, RoundingMode.HALF_UP);
                order.setSubtotal(orderSubtotal);
              if (order.getDiscountCode() != null && !order.getDiscountCode().isEmpty()) {
                    QuoteResponse quote = couponService.validateAndQuote(order.getDiscountCode(), orderSubtotal);
                    if (quote.isValid()) {
                        order.setDiscountAmount(quote.getDiscountAmount());
                        order.setTotal(quote.getNewTotal());
                    } else {
                        throw new IllegalArgumentException(quote.getMessage());
                    }
                } else {
                    order.setTotal(orderSubtotal);
                }
                return order;
            });
    }
}