package tacos.rules;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import tacos.Taco;

@Component
public class IngredientCountRule implements TacoRule {
    @Override
    public List<String> evaluate(Taco taco) {
        if (taco.getIngredients() == null || taco.getIngredients().size() < 2) {
            return Collections.singletonList("MIN_INGREDIENTS_NOT_MET");
        }
        if (taco.getIngredients().size() > 12) {
            return Collections.singletonList("MAX_INGREDIENTS_EXCEEDED");
        }
        return Collections.emptyList();
    }
}