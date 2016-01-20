package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.SkillType;

import org.bukkit.util.NumberConversions;

public class DefaultCalculator implements LevelCalculator {

    //max of 1000 level
    private static final int MAX_LEVEL = 1000;

    @Override
    public int calculateLevel(PlayerProfile mcMMOProfile) {
        int swords = getLevel(mcMMOProfile, SkillType.SWORDS);
        int axes = getLevel(mcMMOProfile, SkillType.AXES);
        int unarmed = getLevel(mcMMOProfile, SkillType.UNARMED);
        int archery = getLevel(mcMMOProfile, SkillType.ARCHERY);
        int taming = getLevel(mcMMOProfile, SkillType.TAMING);
        int acrobatics = getLevel(mcMMOProfile, SkillType.ACROBATICS);

        return NumberConversions.round((unarmed + swords + axes + archery + .25 * acrobatics + .25 * taming) / 45);
    }

    private int getLevel(PlayerProfile mcMMOProfile, SkillType skillType) {
        int skillLevel = mcMMOProfile.getSkillLevel(skillType);
        if (skillLevel <= MAX_LEVEL) {
            return skillLevel;
        }

        return MAX_LEVEL;
    }
}
