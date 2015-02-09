package com.gmail.mrphpfan;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final McCombatLevel pluginInstance;

    public PlayerListener(McCombatLevel plugin) {
        this.pluginInstance = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        pluginInstance.updateLevel(player);

        //send them the scoreboard
        if (pluginInstance.isTagEnabled()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        //remove the player from the hashmap
        pluginInstance.removeCachedLevels(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        //remove the player from the hashmap
        pluginInstance.removeCachedLevels(event.getPlayer());
    }

    //todo make thread-safe
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!pluginInstance.isPrefixEnabled()) {
            //check if prefix is enabled
            return;
        }

        Player player = event.getPlayer();
        //append a level prefix to their name
        Integer combatLevel = pluginInstance.getCombatLevel(player);
        if (combatLevel != null) {
            ChatColor prefixColor = pluginInstance.getPrefixColor();
            ChatColor prefixBracketColor = pluginInstance.getPrefixBracketColor();
            event.setFormat(prefixBracketColor + "[" + prefixColor + combatLevel + prefixBracketColor + "]" + ChatColor.RESET + event.getFormat());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerLevelUp(McMMOPlayerLevelUpEvent event) {
        Player player = event.getPlayer();
        SkillType skill = event.getSkill();

        //only level up combat if one of the following was leveled
        if (skill.equals(SkillType.SWORDS) || skill.equals(SkillType.ARCHERY)
                || skill.equals(SkillType.AXES) || skill.equals(SkillType.UNARMED)
                || skill.equals(SkillType.TAMING) || skill.equals(SkillType.ACROBATICS)) {
            pluginInstance.updateLevel(player);
        }
    }
}
