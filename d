[1mdiff --git a/tacocloud/tacocloud-api/pom.xml b/tacocloud/tacocloud-api/pom.xml[m
[1mindex 2fdea83..f3565e8 100644[m
[1m--- a/tacocloud/tacocloud-api/pom.xml[m
[1m+++ b/tacocloud/tacocloud-api/pom.xml[m
[36m@@ -83,7 +83,12 @@[m
 			<artifactId>spring-boot-starter-test</artifactId>[m
 			<scope>test</scope>[m
 		</dependency>[m
[31m-[m
[32m+[m		[32m<dependency>[m
[32m+[m			[32m<groupId>io.projectreactor</groupId>[m
[32m+[m			[32m<artifactId>reactor-test</artifactId>[m
[32m+[m			[32m<scope>test</scope>[m
[32m+[m			[32m<version>3.6.0</version>[m
[32m+[m		[32m</dependency>[m
 		<!-- <dependency>[m
 			<groupId>org.springframework.boot</groupId>[m
 			<artifactId>spring-boot-starter-artemis</artifactId>[m
[1mdiff --git a/tacocloud/tacocloud-api/src/main/java/tacos/web/api/IngredientController.java b/tacocloud/tacocloud-api/src/main/java/tacos/web/api/IngredientController.java[m
[1mindex b15fd1f..0f8650a 100644[m
[1m--- a/tacocloud/tacocloud-api/src/main/java/tacos/web/api/IngredientController.java[m
[1m+++ b/tacocloud/tacocloud-api/src/main/java/tacos/web/api/IngredientController.java[m
[36m@@ -44,11 +44,15 @@[m [mpublic class IngredientController {[m
   }[m
 [m
   @PutMapping("/{id}")[m
[31m-  public void updateIngredient(@PathVariable String id, @RequestBody Ingredient ingredient) {[m
[32m+[m[32m  public Mono<ResponseEntity<Ingredient>> updateIngredient(@PathVariable String id,@RequestBody Ingredient ingredient) {[m
     if (!ingredient.getId().equals(id)) {[m
[31m-      throw new IllegalStateException("Given ingredient's ID doesn't match the ID in the path.");[m
[32m+[m[32m      return Mono.just(new ResponseEntity<Ingredient>(HttpStatus.BAD_REQUEST));[m
     }[m
[31m-    repo.save(ingredient);[m
[32m+[m[32m     return repo.findById(id).flatMap(existing->{[m
[32m+[m[32m      existing.setName(ingredient.getName());[m
[32m+[m[32m      existing.setType(ingredient.getType());[m
[32m+[m[32m      return repo.save(existing);[m
[32m+[m[32m     }).map(saved->ResponseEntity.ok(saved)).defaultIfEmpty(ResponseEntity.notFound().build());[m
   }[m
 [m
   @PostMapping[m
[1mdiff --git a/tacocloud/tacocloud-api/src/test/java/tacos/web/api/TacoControllerTest.java b/tacocloud/tacocloud-api/src/test/java/tacos/web/api/TacoControllerTest.java[m
[1mindex 6d52c8c..5e73371 100644[m
[1m--- a/tacocloud/tacocloud-api/src/test/java/tacos/web/api/TacoControllerTest.java[m
[1m+++ b/tacocloud/tacocloud-api/src/test/java/tacos/web/api/TacoControllerTest.java[m
[36m@@ -1,5 +1,6 @@[m
 package tacos.web.api;[m
 [m
[32m+[m[32mimport static org.assertj.core.api.Assertions.assertThat;[m
 import static org.mockito.ArgumentMatchers.any;[m
 import static org.mockito.Mockito.when;[m
 [m
[36m@@ -7,19 +8,25 @@[m [mimport java.util.ArrayList;[m
 import java.util.List;[m
 [m
 import org.junit.jupiter.api.Test;[m
[32m+[m[32mimport org.junit.jupiter.api.extension.ExtendWith;[m
[32m+[m[32mimport org.mockito.InjectMocks;[m
[32m+[m[32mimport org.mockito.Mock;[m
 import org.mockito.Mockito;[m
[32m+[m[32mimport org.mockito.junit.jupiter.MockitoExtension;[m
 import org.springframework.http.MediaType;[m
[32m+[m[32mimport org.springframework.http.ResponseEntity;[m
 import org.springframework.test.web.reactive.server.WebTestClient;[m
 [m
 import reactor.core.publisher.Flux;[m
 import reactor.core.publisher.Mono;[m
[32m+[m[32mimport reactor.test.StepVerifier;[m
 import tacos.Ingredient;[m
 import tacos.Ingredient.Type;[m
 import tacos.Taco;[m
[32m+[m[32mimport tacos.data.IngredientRepository;[m
 import tacos.data.TacoRepository;[m
 [m
 public class TacoControllerTest {[m
[31m-[m
   @Test[m
   public void shouldReturnRecentTacos() {[m
     Taco[] tacos = {[m
[36m@@ -90,4 +97,38 @@[m [mpublic class TacoControllerTest {[m
     taco.setIngredients(ingredients);[m
     return taco;[m
   }[m
[32m+[m[32m  @Test[m
[32m+[m[32m  public void testUpdateIngredients_Exito() {[m
[32m+[m[32m    IngredientRepository repo = Mockito.mock(IngredientRepository.class);[m
[32m+[m[32m    IngredientController controller = new IngredientController(repo);[m
[32m+[m[32m    Ingredient ingViejo = new Ingredient("FLTO", "Tortilla Normal", Type.WRAP);[m
[32m+[m[32m    Ingredient ingNuevo = new Ingredient("FLTO", "Tortilla Gigante", Type.WRAP);[m
[32m+[m[41m  [m
[32m+[m[32m    Mockito.when(repo.findById("FLTO")).thenReturn(Mono.just(ingViejo));[m
[32m+[m[32m    Mockito.when(repo.save(Mockito.any(Ingredient.class))).thenReturn(Mono.just(ingNuevo));[m
[32m+[m[32m    Mono<ResponseEntity<Ingredient>> resultado = controller.updateIngredient("FLTO", ingNuevo);[m
[32m+[m[32m    StepVerifier.create(resultado).assertNext(response -> {[m
[32m+[m[32m      assertThat(response.getStatusCodeValue()).isEqualTo(200);[m
[32m+[m[32m      Ingredient ingredientResponse = response.getBody();[m
[32m+[m[32m      assertThat(ingredientResponse).isNotNull();[m
[32m+[m[32m      assertThat(ingredientResponse.getId()).isEqualTo("FLTO");[m
[32m+[m[32m      assertThat(ingredientResponse.getName()).isEqualTo("Tortilla Gigante");[m
[32m+[m[32m    }).verifyComplete();[m
[32m+[m
[32m+[m[32m    Mockito.verify(repo).save(Mockito.any(Ingredient.class));[m
[32m+[m[32m  }[m
[32m+[m
[32m+[m[32m  @Test[m
[32m+[m[32m  public void testUpdateIngredients_NoEncontrado() {[m
[32m+[m[32m    IngredientRepository repo = Mockito.mock(IngredientRepository.class);[m
[32m+[m[32m    IngredientController controller = new IngredientController(repo);[m
[32m+[m[32m    Mockito.when(repo.findById("ID_INVALIDO")).thenReturn(Mono.empty());[m
[32m+[m[32m    Ingredient ingNuevo = new Ingredient("ID_INVALIDO", "Ingrediente Fantasma", Type.WRAP);[m
[32m+[m[32m    Mono<ResponseEntity<Ingredient>> resultado = controller.updateIngredient("ID_INVALIDO", ingNuevo);[m
[32m+[m[32m    StepVerifier.create(resultado).assertNext(response -> {[m
[32m+[m[32m      assertThat(response.getStatusCodeValue()).isEqualTo(404);[m
[32m+[m[32m      assertThat(response.getBody()).isNull();[m
[32m+[m[32m    }).verifyComplete();[m
[32m+[m[32m    Mockito.verify(repo, Mockito.never()).save(Mockito.any(Ingredient.class));[m
[32m+[m[32m  }[m
 }[m
[1mdiff --git a/tacocloud/tacocloud/src/test/java/tacos/DesignTacoControllerBrowserTest.java b/tacocloud/tacocloud/src/test/java/tacos/DesignTacoControllerBrowserTest.java[m
[1mindex de1e116..bb5da10 100644[m
[1m--- a/tacocloud/tacocloud/src/test/java/tacos/DesignTacoControllerBrowserTest.java[m
[1m+++ b/tacocloud/tacocloud/src/test/java/tacos/DesignTacoControllerBrowserTest.java[m
[36m@@ -1,10 +1,8 @@[m
 package tacos;[m
 [m
 import static org.assertj.core.api.Assertions.assertThat;[m
[31m-[m
 import java.util.List;[m
 import java.util.concurrent.TimeUnit;[m
[31m-[m
 import org.junit.jupiter.api.AfterAll;[m
 import org.junit.jupiter.api.BeforeAll;[m
 import org.junit.jupiter.api.Disabled;[m
[36m@@ -12,7 +10,7 @@[m [mimport org.junit.jupiter.api.Test;[m
 import org.junit.jupiter.api.extension.ExtendWith;[m
 import org.openqa.selenium.By;[m
 import org.openqa.selenium.WebElement;[m
[31m-import org.openqa.selenium.chrome.ChromeDriver;[m
[32m+[m[32mimport org.openqa.selenium.htmlunit.HtmlUnitDriver;[m
 import org.springframework.beans.factory.annotation.Autowired;[m
 import org.springframework.boot.test.context.SpringBootTest;[m
 import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;[m
[36m@@ -20,13 +18,11 @@[m [mimport org.springframework.boot.test.web.client.TestRestTemplate;[m
 import org.springframework.boot.web.server.LocalServerPort;[m
 import org.springframework.test.context.junit.jupiter.SpringExtension;[m
 [m
[31m-import io.github.bonigarcia.wdm.WebDriverManager;[m
[31m-[m
 @ExtendWith(SpringExtension.class)[m
 @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)[m
 public class DesignTacoControllerBrowserTest {[m
 [m
[31m-  private static ChromeDriver browser;[m
[32m+[m[32m  private static HtmlUnitDriver browser;[m
 [m
   @LocalServerPort[m
   private int port;[m
[36m@@ -36,10 +32,9 @@[m [mpublic class DesignTacoControllerBrowserTest {[m
 [m
   @BeforeAll[m
   public static void openBrowser() {[m
[31m-    WebDriverManager.chromedriver().setup();[m
[31m-    browser = new ChromeDriver();[m
[31m-    browser.manage().timeouts()[m
[31m-        .implicitlyWait(10, TimeUnit.SECONDS);[m
[32m+[m[32m    // Inicializamos el navegador invisible de Java en lugar de Chrome[m
[32m+[m[32m    browser = new HtmlUnitDriver();[m
[32m+[m[32m    browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);[m
   }[m
 [m
   @AfterAll[m
[36m@@ -75,5 +70,4 @@[m [mpublic class DesignTacoControllerBrowserTest {[m
     assertThat(ingredient.findElement(By.tagName("input")).getAttribute("value")).isEqualTo(id);[m
     assertThat(ingredient.findElement(By.tagName("span")).getText()).isEqualTo(name);[m
   }[m
[31m-[m
[31m-}[m
[32m+[m[32m}[m
\ No newline at end of file[m
