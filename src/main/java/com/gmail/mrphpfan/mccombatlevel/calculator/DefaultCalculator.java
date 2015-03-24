package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import org.bukkit.util.NumberConversions;


public class DefaultCalculator implements LevelCalculator {

    //max of 1000 level
    private static final int MAX_LEVEL = 1000;

    @Override
    public int calculateLevel(McMMOPlayer mcMMOPlayer) {
        int swords = getLevel(mcMMOPlayer, SkillType.SWORDS);
        int axes = getLevel(mcMMOPlayer, SkillType.AXES);
        int unarmed = getLevel(mcMMOPlayer, SkillType.UNARMED);
        int archery = getLevel(mcMMOPlayer, SkillType.ARCHERY);
        int taming = getLevel(mcMMOPlayer, SkillType.TAMING);
        int acrobatics = getLevel(mcMMOPlayer, SkillType.ACROBATICS);

        return NumberConversions.round((unarmed + swords + axes + archery
                    + .25 * acrobatics + .25 * taming) / 45);
    }

    private int getLevel(McMMOPlayer mcMMOPlayer, SkillType skillType) {
        int skillLevel = mcMMOPlayer.getSkillLevel(skillType);
        if (skillLevel <= MAX_LEVEL) {
            return skillLevel;
        }

        return MAX_LEVEL;
    }
}
