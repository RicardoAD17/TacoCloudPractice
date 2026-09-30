package tacos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import tacos.Ingredient;
import tacos.Taco;
import tacos.rules.IngredientCountRule;
import tacos.rules.NoDuplicatesRule;
import tacos.rules.TacoRule;
import tacos.service.TacoValidationService;

public class TacoValidationServiceTest {

    @Test
    public void tacoInvalidoRetornaMultiplesViolaciones() {
        TacoRule countRule = new IngredientCountRule();
        TacoRule duplicatesRule = new NoDuplicatesRule();
        TacoRule fakeRule = taco -> Collections.singletonList("FAKE_RULE_VIOLATION");
        TacoValidationService service = new TacoValidationService(Arrays.asList(countRule, duplicatesRule, fakeRule));
        Taco taco = new Taco();
        Ingredient carnitas = new Ingredient(); carnitas.setId("CARN");
       taco.setIngredients(Collections.singletonList(carnitas));
        List<String> violations = service.validate(taco);
        assertThat(violations).contains("MIN_INGREDIENTS_NOT_MET", "FAKE_RULE_VIOLATION");
        assertThat(violations).doesNotContain("DUPLICATE_INGREDIENTS_NOT_ALLOWED");
    }
}