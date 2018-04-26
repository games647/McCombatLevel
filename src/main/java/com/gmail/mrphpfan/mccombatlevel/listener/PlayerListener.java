package com.gmail.mrphpfan.mccombatlevel.listener;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.mrphpfan.mccombatlevel.task.ProfileWaitingTask;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

import java.util.OptionalInt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private static final String CHAT_VARIABLE = "[combatlevel]";

    protected final McCombatLevel plugin;

    public PlayerListener(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        final Player player = joinEvent.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, new ProfileWaitingTask(plugin, player), 20L);

        //send them the scoreboard
        if (plugin.getScoreboardManger() != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent quitEvent) {
        //remove the player from the cache
        plugin.removeCachedLevels(quitEvent.getPlayer());
    }

    //set it to low in order to update the level before other plugins want to get that value
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerLevelUp(McMMOPlayerLevelUpEvent levelUpEvent) {
        plugin.updateLevel(levelUpEvent.getPlayer());
    }

    //some chat plugins listen and change stuff on the default priority. In order
    //to see these changes we need an higher priority.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent chatEvent) {
        if (!plugin.isPrefixEnabled()) {
            //check if prefix is enabled
            return;
        }

        OptionalInt combatLevel = plugin.getLevel(chatEvent.getPlayer());
        String format = chatEvent.getFormat();
        if (format.contains(CHAT_VARIABLE)) {
            String level = "";
            if (combatLevel.isPresent()) {
                level = Integer.toString(combatLevel.getAsInt());
            }

            chatEvent.setFormat(format.replace(CHAT_VARIABLE, level));
            //variable found - do not append the tag manually
            return;
        }

        //append a level prefix to their name
        if (combatLevel.isPresent()) {
            ChatColor prefixColor = plugin.getPrefixColor();
            ChatColor prefixBracket = plugin.getPrefixBracket();
            chatEvent.setFormat(prefixBracket + "[" + prefixColor + combatLevel.getAsInt() + prefixBracket + "]"
                    + ChatColor.RESET + chatEvent.getFormat());
        }
    }
}
