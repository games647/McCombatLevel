package com.gmail.mrphpfan;

import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Matchers.any;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({PrimarySkillType.class, Config.class})
@RunWith(PowerMockRunner.class)
public class CalculationTest {

   @Before
   public void before() {
       //ignore test in Java 10 in combination with PowerMock, because of bugs
       assumeThat(new ScriptEngineManager().getEngineByName("JavaScript"), notNullValue());

       mockStatic(Config.class);

       Config fakeConfig = mock(Config.class);
       when(Config.getInstance()).thenReturn(fakeConfig);
       when(fakeConfig.getLocale()).thenReturn("en_US");
   }

   @Test
   @Ignore
   public void testScript() {
       String formula = "Math.round((unarmed + swords + axes + archery + .25 * acrobatics + .25 * taming) / 45)";
       LevelCalculator levelCalculator = new JavaScriptCalculator(formula);

       PlayerProfile playerProfile = mock(PlayerProfile.class);
       when(playerProfile.getSkillLevel(any(PrimarySkillType.class))).thenReturn(100);
       assertThat(10, is(levelCalculator.calculateLevel(playerProfile)));
   }
}
