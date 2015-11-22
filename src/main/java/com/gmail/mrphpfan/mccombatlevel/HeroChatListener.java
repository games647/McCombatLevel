package com.gmail.mrphpfan.mccombatlevel;

import com.dthielke.herochat.ChannelChatEvent;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HeroChatListener implements Listener {

    private static final String CHAT_VARIABLE = "[combatlevel]";

    private final McCombatLevel plugin;

    public HeroChatListener(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(ChannelChatEvent chatEvent) {
        if (!plugin.isPrefixEnabled()) {
            //check if prefix is enabled
            return;
        }

        Integer combatLevel = plugin.getCombatLevel(chatEvent.getSender().getPlayer());
        String format = chatEvent.getFormat();
        if (format.contains(CHAT_VARIABLE)) {
            String level = "";
            if (combatLevel != null) {
                level = combatLevel.toString();
            }

            chatEvent.setFormat(format.replace(CHAT_VARIABLE, level));
            //variable found - do not append the tag manually
            return;
        }

        //append a level prefix to their name
        if (combatLevel != null) {
            ChatColor prefixColor = plugin.getPrefixColor();
            ChatColor prefixBracket = plugin.getPrefixBracket();
            chatEvent.setFormat(prefixBracket + "[" + prefixColor + combatLevel + prefixBracket + "]"
                    + ChatColor.RESET + chatEvent.getFormat());
        }
    }
}
