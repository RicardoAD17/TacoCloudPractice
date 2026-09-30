package tacos.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import tacos.Taco;
import tacos.data.TacoRepository;
@Service
public class TacoOfTheDayService {
    private final TacoRepository tacoRepo;
    private final Clock clock;
    public  TacoOfTheDayService(TacoRepository tacoRepo, Clock clock){
        this.tacoRepo=tacoRepo;
        this.clock=clock;
    }
    public Mono<Taco> getTacoOfTheDay(){
        return tacoRepo.count().flatMap(total->{
            if(total==0){
                return Mono.empty();
            }

            LocalDate today= LocalDate.now(clock);
            long index = Math.abs((long) today.hashCode())% total;
            return  tacoRepo.findAll().skip(index).next();
        });
    }
}
