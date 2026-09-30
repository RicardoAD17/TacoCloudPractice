package tacos.ops;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.User;

import javax.validation.Valid;
import java.util.Date;

@Component
@RestControllerEndpoint(id = "announcements") 
public class OpsAnnouncementEndpoint {

    private final OpsAnnouncementRepository repo;
    private static final int MAX_ACTIVE_ANNOUNCEMENTS = 5; 

    public OpsAnnouncementEndpoint(OpsAnnouncementRepository repo) {
        this.repo = repo;
    }
    @GetMapping
    public Flux<AnnouncementPublicDTO> getActiveAnnouncements() {
        return repo.findByActiveTrueAndExpiresAtAfter(new Date())
                .map(ann -> new AnnouncementPublicDTO(
                        ann.getId(), 
                        ann.getText(), 
                        ann.getSeverity(), 
                        ann.getExpiresAt()
                ));
    }

    @PostMapping
    public Mono<ResponseEntity<OpsAnnouncement>> createAnnouncement(
            @Valid @RequestBody OpsAnnouncement announcement,
            @AuthenticationPrincipal User user) {

        return repo.findByActiveTrueAndExpiresAtAfter(new Date()).count()
                .flatMap(count -> {
                    if (count >= MAX_ACTIVE_ANNOUNCEMENTS) {
                        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).<OpsAnnouncement>build());
                    }

                    announcement.setCreatedBy(user != null ? user.getUsername() : "SYSTEM");
                    announcement.setCreatedAt(new Date());
                    announcement.setActive(true);

                    return repo.save(announcement)
                            .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
                });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAnnouncement(@PathVariable String id) {
        return repo.findById(id)
                .flatMap(ann -> repo.delete(ann).then(Mono.just(ResponseEntity.noContent().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    public static class AnnouncementPublicDTO {
        public String id;
        public String text;
        public String severity;
        public Date expiresAt;

        public AnnouncementPublicDTO(String id, String text, String severity, Date expiresAt) {
            this.id = id;
            this.text = text;
            this.severity = severity;
            this.expiresAt = expiresAt;
        }
    }
}