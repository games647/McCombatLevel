package com.gmail.mrphpfan;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final McCombatLevel pluginInstance;

    public LevelCommand(McCombatLevel plugin) {
        this.pluginInstance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("combatlevel")) {
            int level = pluginInstance.getCombatLevel((Player) sender);
            sender.sendMessage(ChatColor.GOLD + "Combat level: " + ChatColor.DARK_GREEN + level);
        }

        return true;
    }
}
