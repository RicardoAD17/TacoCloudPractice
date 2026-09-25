package tacos.rules;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import tacos.Taco;
import tacos.Ingredient;

@Component
public class NoDuplicatesRule implements TacoRule {
    @Override
    public List<String> evaluate(Taco taco) {
        if (taco.getIngredients() == null) return Collections.emptyList();
        
        Set<String> uniqueIds = new HashSet<>();
        for (Ingredient ing : taco.getIngredients()) {
            if (!uniqueIds.add(ing.getId())) {
                return Collections.singletonList("DUPLICATE_INGREDIENTS_NOT_ALLOWED");
            }
        }
        return Collections.emptyList();
    }
}