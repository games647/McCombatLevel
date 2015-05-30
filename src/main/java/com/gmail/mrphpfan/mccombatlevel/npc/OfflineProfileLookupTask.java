package com.gmail.mrphpfan.mccombatlevel.npc;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.mcMMO;

import org.bukkit.Bukkit;

public class OfflineProfileLookupTask implements Runnable {

    private final McCombatLevel plugin;
    private final String playerName;
    private final int defaultLevel;

    public OfflineProfileLookupTask(String playerName, McCombatLevel plugin, int defaultLevel) {
        this.plugin = plugin;
        this.playerName = playerName;
        this.defaultLevel = defaultLevel;
    }

    @Override
    public void run() {
        //there is no such player online -> offline lookup (this call should be async because it's blocking)
        PlayerProfile offlineProfile = mcMMO.getDatabaseManager().loadPlayerProfile(playerName, false);
        Bukkit.getScheduler().runTask(plugin, new NpcCalculatorTask(plugin, offlineProfile, defaultLevel));
    }
}
