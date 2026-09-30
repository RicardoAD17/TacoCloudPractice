package tacos.rules;

import java.util.List;

import tacos.Taco;

public interface TacoRule {
    List<String> evaluate(Taco taco);
}
