package com.gmail.mrphpfan.mccombatlevel.calculator;

import com.gmail.nossr50.datatypes.player.PlayerProfile;

@FunctionalInterface
public interface LevelCalculator {

    int calculateLevel(PlayerProfile mcMMOProfile);
}
