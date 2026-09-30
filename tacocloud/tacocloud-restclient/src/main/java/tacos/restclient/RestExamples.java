package tacos.restclient;

import java.net.URI;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import tacos.Ingredient;

@SpringBootConfiguration
@ComponentScan
@Slf4j
public class RestExamples {

  public static void main(String[] args) {
    SpringApplication.run(RestExamples.class, args);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
  
  @Bean
  public CommandLineRunner fetchIngredients(TacoCloudClient tacoCloudClient) {
    return args -> {
      log.info("----------------------- GET -------------------------");
      log.info("GETTING INGREDIENT BY IDE");
      log.info("Ingredient:  " + tacoCloudClient.getIngredientById("CHED"));
      log.info("GETTING ALL INGREDIENTS");
      List<Ingredient> ingredients = tacoCloudClient.getAllIngredients();
      log.info("All ingredients:");
      for (Ingredient ingredient : ingredients) {
        log.info("   - " + ingredient);
      }
    };
  }
  
  @Bean
  public CommandLineRunner putAnIngredient(TacoCloudClient tacoCloudClient) {
    return args -> {
      log.info("----------------------- PUT -------------------------");
      Ingredient before = tacoCloudClient.getIngredientById("LETC");
      log.info("BEFORE:  " + before);
      
      // Corrección aquí
      Ingredient updatedLettuce = new Ingredient();
      updatedLettuce.setId("LETC");
      updatedLettuce.setName("Shredded Lettuce");
      updatedLettuce.setType(Ingredient.Type.VEGGIES);
      updatedLettuce.setUnitPrice(java.math.BigDecimal.ZERO);
      
      tacoCloudClient.updateIngredient(updatedLettuce);
      Ingredient after = tacoCloudClient.getIngredientById("LETC");
      log.info("AFTER:  " + after);
    };
  }
  
  @Bean
  public CommandLineRunner addAnIngredient(TacoCloudClient tacoCloudClient) {
    return args -> {
      log.info("----------------------- POST -------------------------");
      
      // Corrección aquí (CHIX)
      Ingredient chix = new Ingredient();
      chix.setId("CHIX");
      chix.setName("Shredded Chicken");
      chix.setType(Ingredient.Type.PROTEIN);
      chix.setUnitPrice(java.math.BigDecimal.ZERO);
      
      Ingredient chixAfter = tacoCloudClient.createIngredient(chix);
      log.info("AFTER-1:  " + chixAfter);
      
      // Corrección aquí (BFFJ)
      Ingredient beefFajita = new Ingredient();
      beefFajita.setId("BFFJ");
      beefFajita.setName("Beef Fajita");
      beefFajita.setType(Ingredient.Type.PROTEIN);
      beefFajita.setUnitPrice(java.math.BigDecimal.ZERO);
      
      URI uri = tacoCloudClient.createIngredient2(beefFajita);
      log.info("AFTER-2:  " + uri);      
      
      // Corrección aquí (SHMP)
      Ingredient shrimp = new Ingredient();
      shrimp.setId("SHMP");
      shrimp.setName("Shrimp");
      shrimp.setType(Ingredient.Type.PROTEIN);
      shrimp.setUnitPrice(java.math.BigDecimal.ZERO);
      
      Ingredient shrimpAfter = tacoCloudClient.createIngredient3(shrimp);
      log.info("AFTER-3:  " + shrimpAfter);      
    };
  }

  
  @Bean
  public CommandLineRunner deleteAnIngredient(TacoCloudClient tacoCloudClient) {
    return args -> {
      log.info("----------------------- DELETE -------------------------");
      Ingredient before = tacoCloudClient.getIngredientById("CHIX");
      log.info("BEFORE:  " + before);
      tacoCloudClient.deleteIngredient(before);
      Ingredient after = tacoCloudClient.getIngredientById("CHIX");
      log.info("AFTER:  " + after);
      before = tacoCloudClient.getIngredientById("BFFJ");
      log.info("BEFORE:  " + before);
      tacoCloudClient.deleteIngredient(before);
      after = tacoCloudClient.getIngredientById("BFFJ");
      log.info("AFTER:  " + after);
      before = tacoCloudClient.getIngredientById("SHMP");
      log.info("BEFORE:  " + before);
      tacoCloudClient.deleteIngredient(before);
      after = tacoCloudClient.getIngredientById("SHMP");
      log.info("AFTER:  " + after);
    };
  }

}