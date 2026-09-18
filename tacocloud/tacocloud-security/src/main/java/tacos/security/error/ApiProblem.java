package tacos.security.error;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder 
@Getter
@Setter
@JsonInclude
public class ApiProblem {
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private String code;
    private List<Violation> violations;
    @Getter
    @Setter
    public static class Violation {
        private String field;
        private String message;

        public Violation(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
