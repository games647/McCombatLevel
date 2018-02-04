package com.gmail.mrphpfan;

import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;

import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({SkillType.class, Config.class})
@RunWith(PowerMockRunner.class)
public class CalculationTest {

   private String formula;

   @Before
   public void before() {
       mockStatic(Config.class);

       Config fakeConfig = mock(Config.class);
       when(Config.getInstance()).thenReturn(fakeConfig);
       when(fakeConfig.getLocale()).thenReturn("en_US");

       InputStream resourceAsStream = getClass().getResourceAsStream("/config.yml");
       InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
       formula = YamlConfiguration.loadConfiguration(inputStreamReader).getString("formula");
   }

   @Test
   public void skillTypeUse() {
       for (SkillType combatSkill : SkillType.COMBAT_SKILLS) {
           //test if the default formula contains all combat variables
           assertThat(formula, containsString(combatSkill.getName().toLowerCase()));
       }
   }

   @Test
   public void testScript() {
       LevelCalculator levelCalculator = new JavaScriptCalculator(formula);

       PlayerProfile playerProfile = mock(PlayerProfile.class);
       when(playerProfile.getSkillLevel(any(SkillType.class))).thenReturn(100);
       assertThat(10, is(levelCalculator.calculateLevel(playerProfile)));
   }
}
