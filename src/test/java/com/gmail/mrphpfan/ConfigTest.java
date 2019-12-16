package com.gmail.mrphpfan;

import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import java.io.IOException;
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

import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({PrimarySkillType.class, Config.class})
@RunWith(PowerMockRunner.class)
public class ConfigTest {

    private String formula;

    @Before
    public void before() throws IOException {
        mockStatic(Config.class);

        Config fakeConfig = mock(Config.class);
        when(Config.getInstance()).thenReturn(fakeConfig);
        when(fakeConfig.getLocale()).thenReturn("en_US");

        InputStream resourceAsStream = getClass().getResourceAsStream("/config.yml");
        try (InputStreamReader inputStreamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)) {
            formula = YamlConfiguration.loadConfiguration(inputStreamReader).getString("formula");
        }
    }

    @Test
    public void skillTypeUse() {
        for (PrimarySkillType combatSkill : PrimarySkillType.COMBAT_SKILLS) {
            //test if the default formula contains all combat variables
            assertThat(formula, containsString(combatSkill.name().toLowerCase()));
        }
    }
}
