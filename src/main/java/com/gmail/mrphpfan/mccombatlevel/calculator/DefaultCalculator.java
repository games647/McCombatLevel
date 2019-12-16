package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;

import org.bukkit.util.NumberConversions;

public class DefaultCalculator implements LevelCalculator {

    //max of 1000 level
    private static final int MAX_LEVEL = 1000;

    @Override
    public int calculateLevel(PlayerProfile mcMMOProfile) {
        int swords = getLevel(mcMMOProfile, PrimarySkillType.SWORDS);
        int axes = getLevel(mcMMOProfile, PrimarySkillType.AXES);
        int unarmed = getLevel(mcMMOProfile, PrimarySkillType.UNARMED);
        int archery = getLevel(mcMMOProfile, PrimarySkillType.ARCHERY);
        int taming = getLevel(mcMMOProfile, PrimarySkillType.TAMING);
        int acrobatics = getLevel(mcMMOProfile, PrimarySkillType.ACROBATICS);

        double sum = unarmed + swords + axes + archery + 0.25 * acrobatics + 0.25 * taming;
        return NumberConversions.round(sum / 45);
    }

    private int getLevel(PlayerProfile mcMMOProfile, PrimarySkillType skillType) {
        int skillLevel = mcMMOProfile.getSkillLevel(skillType);
        return Math.min(skillLevel, MAX_LEVEL);
    }
}
