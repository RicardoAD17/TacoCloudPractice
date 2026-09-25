package tacos.web.api;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

import tacos.Ingredient;

import tacos.Taco;
import tacos.model.Allergen;
import tacos.model.DietaryTag;
import tacos.model.SpiceLevel;

@Service
public class TacoClassificationService {

    public void classifyTaco(Taco taco) {
        // Si el taco no tiene ingredientes, no tiene clasificación
        if (taco.getIngredients() == null || taco.getIngredients().isEmpty()) {
            taco.setDietaryTags(EnumSet.noneOf(DietaryTag.class));
            taco.setAllergens(EnumSet.noneOf(Allergen.class));
            taco.setSpiceLevel(SpiceLevel.NONE);
            return;
        }

        // Asumimos que es 100% puro al inicio...
        boolean isVegan = true;
        boolean isVegetarian = true;
        boolean isGlutenFree = true;
        
        Set<Allergen> tacoAllergens = new HashSet<>();
        int maxSpiceRank = 0;

        // ... y evaluamos cada ingrediente para ver si lo arruina
        for (Ingredient ing : taco.getIngredients()) {
            
            // Regla: Si un solo ingrediente no es vegano, el taco ya no es vegano.
            if (!ing.isVegan()) isVegan = false;
            if (!ing.isVegetarian()) isVegetarian = false;
            if (!ing.isGlutenFree()) isGlutenFree = false;

            // Regla: Los alérgenos se unen (Set Union)
            if (ing.getAllergens() != null) {
                tacoAllergens.addAll(ing.getAllergens());
            }

            // Regla: El picante del taco es el máximo picante de sus ingredientes
            if (ing.getSpiceRank() > maxSpiceRank) {
                maxSpiceRank = ing.getSpiceRank();
            }
        }

        Set<DietaryTag> tags = EnumSet.noneOf(DietaryTag.class);
        if (isVegan) tags.add(DietaryTag.VEGAN);
        if (isVegetarian) tags.add(DietaryTag.VEGETARIAN);
        if (isGlutenFree) tags.add(DietaryTag.GLUTEN_FREE);

        taco.setDietaryTags(tags);
        taco.setAllergens(tacoAllergens);
        taco.setSpiceLevel(SpiceLevel.fromRank(maxSpiceRank));
    }
}