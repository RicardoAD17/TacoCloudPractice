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
        boolean isVegan = true;
        boolean isVegetarian = true;
        boolean isGlutenFree = true;
        
        Set<Allergen> tacoAllergens = new HashSet<>();
        int maxSpiceRank = 0;

        for (Ingredient ing : taco.getIngredients()) {
            
           
            if (!ing.isVegan()) isVegan = false;
            if (!ing.isVegetarian()) isVegetarian = false;
            if (!ing.isGlutenFree()) isGlutenFree = false;

            if (ing.getAllergens() != null) {
                tacoAllergens.addAll(ing.getAllergens());
            }
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