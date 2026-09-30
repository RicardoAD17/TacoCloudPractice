package tacos.web.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tacos.Taco;
import tacos.data.TacoRepository;
import tacos.service.TacoOfTheDayService;

public class TacoOfTheDayServiceTest {

    @Test
    public void getTacoOfTheDay_ReturnsSameTaco_OnSameDay() {
        
        Instant fixedInstant = Instant.parse("2026-09-24T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
        TacoRepository mockRepo = mock(TacoRepository.class);
        Taco taco1 = new Taco(); taco1.setName("Taco Uno");
        Taco taco2 = new Taco(); taco2.setName("Taco Dos");
        Taco taco3 = new Taco(); taco3.setName("Taco Tres");

        when(mockRepo.count()).thenReturn(Mono.just(3L));
        when(mockRepo.findAll()).thenReturn(Flux.just(taco1, taco2, taco3));

        TacoOfTheDayService service = new TacoOfTheDayService(mockRepo, fixedClock);

        StepVerifier.create(service.getTacoOfTheDay())
                .expectNextMatches(taco -> taco.getName().equals("Taco Uno") || taco.getName().equals("Taco Dos") || taco.getName().equals("Taco Tres"))
                .verifyComplete();
    }
    
}