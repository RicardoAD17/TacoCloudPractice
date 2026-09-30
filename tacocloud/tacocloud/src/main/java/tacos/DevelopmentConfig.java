package tacos;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import tacos.Ingredient.Type;
import tacos.data.IngredientRepository;
import tacos.data.PaymentMethodRepository;
import tacos.data.TacoRepository;
import tacos.data.UserRepository;

@Profile("!prod")
@Configuration
public class DevelopmentConfig {

  @Bean
  public CommandLineRunner dataLoader(IngredientRepository repo,
        UserRepository userRepo, PasswordEncoder encoder, TacoRepository tacoRepo,
        PaymentMethodRepository paymentMethodRepo) { // user repo for ease of testing with a built-in user
    
    return new CommandLineRunner() {
      @Override
      public void run(String... args) throws Exception {
        Ingredient flourTortilla = saveAnIngredient("FLTO", "Flour Tortilla", Type.WRAP, new java.math.BigDecimal("10.00"), 100);
        Ingredient cornTortilla = saveAnIngredient("COTO", "Corn Tortilla", Type.WRAP, new java.math.BigDecimal("12.00"), 100);
        Ingredient groundBeef = saveAnIngredient("GRBF", "Ground Beef", Type.PROTEIN, new java.math.BigDecimal("25.50"), 50);
        Ingredient carnitas = saveAnIngredient("CARN", "Carnitas", Type.PROTEIN, new java.math.BigDecimal("28.00"), 40);
        Ingredient tomatoes = saveAnIngredient("TMTO", "Diced Tomatoes", Type.VEGGIES, new java.math.BigDecimal("5.00"), 200);
        Ingredient lettuce = saveAnIngredient("LETC", "Lettuce", Type.VEGGIES, new java.math.BigDecimal("4.50"), 200);
        Ingredient cheddar = saveAnIngredient("CHED", "Cheddar", Type.CHEESE, new java.math.BigDecimal("8.00"), 80);
        Ingredient jack = saveAnIngredient("JACK", "Monterrey Jack", Type.CHEESE, new java.math.BigDecimal("9.00"), 80);
        Ingredient salsa = saveAnIngredient("SLSA", "Salsa", Type.SAUCE, new java.math.BigDecimal("3.50"), 150);
        Ingredient sourCream = saveAnIngredient("SRCR", "Sour Cream", Type.SAUCE, new java.math.BigDecimal("4.00"), 150);        
    
        User savedUser = new User("ricardo_admin", encoder.encode("password"), 
              "Ricardo Almada", "123 North Street", "Cross Roads", "TX", 
              "76227", "123-123-1234", "craig@habuma.com");
        savedUser.setRole("ROLE_USER");
        savedUser = userRepo.save(savedUser).block();
        User admin = new User("jefe", encoder.encode("password"), 
              "Jefe de Tienda", "123 Admin St", "Cross Roads", "TX", 
              "76227", "111-222-3333", "jefe@habuma.com");
        admin.setRole("ROLE_ADMIN"); 
        userRepo.save(admin).block();
        if (savedUser != null) {
            paymentMethodRepo.save(new PaymentMethod(savedUser, "tok_falso_habuma_123", "VISA", "1111", "10/25")).block();
            System.out.println(" USUARIO CREADO CON ÉXITO: " + savedUser.getUsername());
        }
        Taco taco1 = new Taco();
        taco1.setId("TACO1");
        taco1.setName("Carnivore");
        taco1.setIngredients(Arrays.asList(flourTortilla, groundBeef, carnitas, sourCream, salsa, cheddar));
        tacoRepo.save(taco1).subscribe();

        Taco taco2 = new Taco();
        taco2.setId("TACO2");
        taco2.setName("Bovine Bounty");
        taco2.setIngredients(Arrays.asList(cornTortilla, groundBeef, cheddar, jack, sourCream));
        tacoRepo.save(taco2).subscribe();

        Taco taco3 = new Taco();
        taco3.setId("TACO3");
        taco3.setName("Veg-Out");
        taco3.setIngredients(Arrays.asList(cornTortilla, tomatoes, lettuce, salsa));
        tacoRepo.save(taco3).subscribe();

      }

      private Ingredient saveAnIngredient(String id, String name, Type type,BigDecimal unitPrice,int stockOnHand) {
        Ingredient ingredient = new Ingredient(id, name, type,unitPrice,stockOnHand);
        repo.save(ingredient).subscribe();
        return ingredient;
      }
    };
  }
  
}