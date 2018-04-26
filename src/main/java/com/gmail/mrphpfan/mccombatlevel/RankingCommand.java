package com.gmail.mrphpfan.mccombatlevel;

import com.gmail.mrphpfan.mccombatlevel.task.LeaderboardReadTask;
import com.google.common.primitives.Ints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RankingCommand implements CommandExecutor {

    private final McCombatLevel plugin;

    public RankingCommand(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.getLeaderboardUpdateTask() == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Ranking is disabled");
            return true;
        }

        if (args.length > 0) {
            Integer parsedPage = Ints.tryParse(args[0]);
            if (parsedPage == null) {
                sender.sendMessage(ChatColor.DARK_RED + "You entered not a valid page number");
            } else {
                queueReadRequest(sender, parsedPage);
            }
        }

        int page = 1;
        queueReadRequest(sender, page);
        return true;
    }

    private void queueReadRequest(CommandSender sender, int page) {
        LeaderboardReadTask leaderboardReadTask = new LeaderboardReadTask(plugin, sender, page);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, leaderboardReadTask);
    }
}
