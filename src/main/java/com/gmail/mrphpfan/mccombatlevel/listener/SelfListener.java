package com.gmail.mrphpfan.mccombatlevel.listener;

import com.gmail.mrphpfan.mccombatlevel.Effects;
import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.mrphpfan.mccombatlevel.PlayerCombatLevelChangeEvent;
import com.gmail.mrphpfan.mccombatlevel.CombatScoreboard;

import java.text.MessageFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SelfListener implements Listener {

    private final McCombatLevel plugin;

    public SelfListener(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCombatLevelChange(PlayerCombatLevelChangeEvent levelChangeEvent) {
        Player player = levelChangeEvent.getPlayer();
        String playerName = player.getName();

        int oldLevel = levelChangeEvent.getOldLevel();
        int newLevel = levelChangeEvent.getNewLevel();

        String levelUpMessage = plugin.getLevelUpMessage();
        if (!levelUpMessage.isEmpty()) {
            String formatMessage = MessageFormat.format(levelUpMessage, oldLevel, newLevel);
            player.sendMessage(formatMessage);
        }

        String broadcastMessage = plugin.getBroadcastMessage();
        if (!broadcastMessage.isEmpty()) {
            String formatMessage = MessageFormat.format(broadcastMessage, oldLevel, newLevel);
            Bukkit.broadcastMessage(formatMessage);
        }

        //map the player's name to the level
        plugin.getPlayerLevels().put(playerName, newLevel);

        //play effects only if there is change
        Effects effects = plugin.getEffects();
        if (oldLevel != -1 && effects != null
                && player.hasPermission(plugin.getName().toLowerCase().toLowerCase() + ".effect")) {
            effects.playEffect(player);
        }

        CombatScoreboard scoreboardManger = plugin.getScoreboardManger();
        if (scoreboardManger != null
                && player.hasPermission(plugin.getName().toLowerCase().toLowerCase() + ".showLevelTag")) {
            scoreboardManger.setScore(playerName, newLevel);
        }

        if (plugin.getLeaderboardUpdateTask() != null) {
            //ranking is enabled
            plugin.getLeaderboardUpdateTask().addToSave(player, newLevel);
        }
    }
}
