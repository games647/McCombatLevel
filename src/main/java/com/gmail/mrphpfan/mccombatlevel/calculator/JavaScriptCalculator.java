package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;

import java.util.Map;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static java.util.stream.Collectors.toMap;

public class JavaScriptCalculator implements LevelCalculator {

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final String formula;

    public JavaScriptCalculator(String formula) {
        this.formula = formula;
    }

    @Override
    public int calculateLevel(PlayerProfile mcMMOProfile) {
        Bindings variables = scriptEngine.createBindings();

        Map<String, Integer> collect = Stream.of(SkillType.values())
                .collect(toMap(skill -> skill.name().toLowerCase(), mcMMOProfile::getSkillLevel));
        variables.putAll(collect);

        try {
            Object result = scriptEngine.eval(formula, variables);
            if (result instanceof Number) {
                return ((Number) result).intValue();
            } else {
                throw new RuntimeException("Formula doesn't returned a number");
            }
        } catch (ScriptException ex) {
            throw new RuntimeException("Combat level cannot be calculated", ex.getCause());
        }
    }

    public boolean isScriptEnabled() {
        return scriptEngine != null;
    }
}
