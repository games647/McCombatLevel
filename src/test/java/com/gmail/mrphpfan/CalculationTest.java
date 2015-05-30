package com.gmail.mrphpfan;

import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.google.common.base.Charsets;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({SkillType.class, Config.class})
@RunWith(PowerMockRunner.class)
public class CalculationTest {

    private String formula;

    @Before
    public void loadFormula() {
        final InputStream resourceAsStream = getClass().getResourceAsStream("/config.yml");
        final InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, Charsets.UTF_8);
        formula =  YamlConfiguration.loadConfiguration(inputStreamReader).getString("formula");
    }

    @Test
    public void skillTypeUse() {
        PowerMockito.mockStatic(Config.class);

        Config fakeConfig = PowerMockito.mock(Config.class);
        PowerMockito.when(Config.getInstance()).thenReturn(fakeConfig);

        PowerMockito.when(fakeConfig.getLocale()).thenReturn("en_US");

        for (SkillType combatSkill : SkillType.COMBAT_SKILLS) {
            //test if the default formula contains all combat variables
            Assert.assertTrue(formula.contains(combatSkill.getName().toLowerCase()));
        }
    }

    @Test
    public void testScript() {
        LevelCalculator levelCalculator = new JavaScriptCalculator(formula);

        PlayerProfile playerProfile = PowerMockito.mock(PlayerProfile.class);
        PowerMockito.when(playerProfile.getSkillLevel(Matchers.any(SkillType.class))).thenReturn(100);
        System.out.println(levelCalculator.calculateLevel(playerProfile));
    }
}
