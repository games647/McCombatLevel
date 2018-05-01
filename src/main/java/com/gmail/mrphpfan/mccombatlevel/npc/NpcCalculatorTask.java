package com.gmail.mrphpfan.mccombatlevel.npc;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.player.PlayerProfile;

public class NpcCalculatorTask implements Runnable {

    private final McCombatLevel plugin;
    private final PlayerProfile offlineProfile;
    private final int defaultLevel;

    public NpcCalculatorTask(McCombatLevel plugin, PlayerProfile offlineProfile, int defaultLevel) {
        this.plugin = plugin;
        this.offlineProfile = offlineProfile;
        this.defaultLevel = defaultLevel;
    }

    @Override
    public void run() {
        int level;
        if (offlineProfile.isLoaded()) {
            //offline player successful found
            level = plugin.calculateLevel(offlineProfile);
        } else {
            level = defaultLevel;
        }

        if (level != 0) {
            plugin.getScoreboardManger().setScore(offlineProfile.getPlayerName(), level);
        }
    }
}
