package com.gmail.mrphpfan.mccombatlevel.npc;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {

    private final McCombatLevel plugin;

    private final int defaultValue;
    private final boolean lookupOffline;

    public NPCListener(McCombatLevel plugin, ConfigurationSection configurationSection) {
        this.plugin = plugin;

        defaultValue = configurationSection.getInt("default");
        lookupOffline = configurationSection.getBoolean("lookupOffline");
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCSpawn(NPCSpawnEvent npcSpawnEvent) {
        String npcName = npcSpawnEvent.getNPC().getName();
        if (lookupOffline && !isPlayerOnline(npcName)) {
            OfflineProfileLookupTask lookupTask = new OfflineProfileLookupTask(npcName, plugin, defaultValue);
            //lookup and add
            Bukkit.getScheduler().runTaskAsynchronously(plugin, lookupTask);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCDeSpawn(NPCDespawnEvent npcDeSpawnEvent) {
        String npcName = npcDeSpawnEvent.getNPC().getName();
        if (!isPlayerOnline(npcName)) {
            //remove from list
            plugin.getScoreboardManger().remove(npcName);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCDeath(NPCDeathEvent npcDeathEvent) {
        //if the npc doesn't respawn we can remove the npc score. The Respawn will be called, but no despawn event
        String npcName = npcDeathEvent.getNPC().getName();
        if (!isPlayerOnline(npcName)) {
            //remove from list
            plugin.getScoreboardManger().remove(npcName);
        }
    }

    public boolean existsNPC(String name) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPlayerOnline(String playerName) {
        //the player score already exists, so don't overwrite it
        return Bukkit.getPlayerExact(playerName) != null;
    }
}
