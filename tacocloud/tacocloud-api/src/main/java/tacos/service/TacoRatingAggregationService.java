package tacos.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import lombok.Data;

@Service
public class TacoRatingAggregationService {
    private final ReactiveMongoTemplate mongoTemplate;

    public TacoRatingAggregationService(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Data
    public static class TopTacoResult {
        private String id; 
        private double averageScore;
        private long totalVotes;
    }

    public Flux<TopTacoResult> getTopTacos(int limit, int minVotes) {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group("tacoId")
                .avg("score").as("averageScore")
                .count().as("totalVotes"),
            Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("totalVotes").gte(minVotes)),
            Aggregation.sort(Sort.Direction.DESC, "averageScore", "totalVotes"),
            Aggregation.limit(limit)
        );
        return mongoTemplate.aggregate(aggregation, "tacoRating", TopTacoResult.class);
    }
}