package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptCalculator implements LevelCalculator {

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private final String formula;

    public JavaScriptCalculator(String formula) {
        this.formula = formula;
    }

    @Override
    public int calculateLevel(McMMOPlayer mcMMOPlayer) {
        Bindings variables = scriptEngine.createBindings();
        for (SkillType skillType : SkillType.values()) {
            //create variables for scripting
            variables.put(skillType.toString().toLowerCase(), mcMMOPlayer.getSkillLevel(skillType));
        }

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
