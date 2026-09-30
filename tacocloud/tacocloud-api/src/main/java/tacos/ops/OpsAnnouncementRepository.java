package tacos.ops;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Date;

public interface OpsAnnouncementRepository extends ReactiveCrudRepository<OpsAnnouncement, String> {

    Flux<OpsAnnouncement> findByActiveTrueAndExpiresAtAfter(Date now);
}