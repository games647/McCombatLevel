package com.gmail.mrphpfan.mccombatlevel;

import java.util.OptionalInt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {

    private final McCombatLevel plugin;

    public LevelCommand(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            return onLevelOther(sender, args[0]);
        }

        return onLevelSelf(sender);
    }

    private boolean onLevelOther(CommandSender sender, String target) {
        if (checkPermission(sender, ".levelcommand.others")) {
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Player: '" + target + "' not found");
            return true;
        }

        OptionalInt level = plugin.getLevel(targetPlayer);
        if (level.isPresent()) {
            sender.sendMessage(ChatColor.GOLD + targetPlayer.getName() + "'s Combat level: "
                    + ChatColor.DARK_GREEN + level.getAsInt());
        } else {
            sender.sendMessage(ChatColor.RED + "Not loaded yet");
        }

        return true;
    }

    private boolean onLevelSelf(CommandSender sender) {
        if (checkPermission(sender, ".levelcommand.self")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            //non player cannot have a combat level
            sender.sendMessage(ChatColor.DARK_RED + "You must be a player to execute this command");
            return true;
        }

        OptionalInt level = plugin.getLevel((Player) sender);
        if (level.isPresent()) {
            sender.sendMessage(ChatColor.GOLD + "Combat level: " + ChatColor.DARK_GREEN + level);
        } else {
            sender.sendMessage(ChatColor.RED + "Not loaded yet");
        }

        return true;
    }

    private boolean checkPermission(CommandSender sender, String permissionNode) {
        if (!sender.hasPermission(plugin.getName().toLowerCase() + permissionNode)) {
            sender.sendMessage(ChatColor.DARK_RED
                    + "You don't have enough permission to see the combat level by command");
            return true;
        }

        return false;
    }
}
