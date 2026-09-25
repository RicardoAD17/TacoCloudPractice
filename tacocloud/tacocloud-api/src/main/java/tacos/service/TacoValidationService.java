package tacos.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import tacos.Taco;
import tacos.rules.TacoRule;

@Service
public class TacoValidationService {

    private final List<TacoRule> rules;

    public TacoValidationService(List<TacoRule> rules) {
        this.rules = rules;
    }

    public List<String> validate(Taco taco) {
        return rules.stream()
                .flatMap(rule -> rule.evaluate(taco).stream())
                .collect(Collectors.toList());
    }
}