package com.gmail.mrphpfan;

import org.bukkit.Bukkit;
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
        if (args.length >= 1) {
            return onLevelOther(sender, args[0]);
        }

        return onLevelSelf(sender);
    }

    private boolean onLevelOther(CommandSender sender, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Player: '" + target + "' not found");
            return true;
        }

        int level = pluginInstance.getCombatLevel(targetPlayer);
        sender.sendMessage(ChatColor.GOLD + targetPlayer.getName() + "'s Combat level: " + ChatColor.DARK_GREEN + level);
        return true;
    }

    private boolean onLevelSelf(CommandSender sender) {
        if (!(sender instanceof Player)) {
            //non player cannot have a combat level
            sender.sendMessage(ChatColor.DARK_RED + "You must be a player to execute this command");
            return true;
        }

        int level = pluginInstance.getCombatLevel((Player) sender);
        sender.sendMessage(ChatColor.GOLD + "Combat level: " + ChatColor.DARK_GREEN + level);
        return true;
    }
}
