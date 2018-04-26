package com.gmail.mrphpfan.mccombatlevel.task;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.NumberConversions;

public class LeaderboardReadTask implements Runnable {

    private final McCombatLevel plugin;
    private final CommandSender sender;
    private final int requestedPage;

    public LeaderboardReadTask(McCombatLevel plugin, CommandSender sender, int requestedPage) {
        this.plugin = plugin;
        this.sender = sender;
        this.requestedPage = requestedPage;
    }

    @Override
    public void run() {
        Path file = plugin.getDataFolder().toPath().resolve("leaderboardIndex.txt");
        if (Files.exists(file)) {
            int startIndex = (requestedPage - 1) * 10 + 1;

            readLeaderboard(file, startIndex, startIndex + 10);
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Leaderboard is not generated yet");
        }
    }

    private void readLeaderboard(Path leaderboardFile, int startPos, int endPos) {
        try {
            List<PlayerStat> results = Lists.newArrayListWithExpectedSize(startPos - endPos);
            int position = 1;

            try (BufferedReader reader = Files.newBufferedReader(leaderboardFile)) {
                plugin.getLeaderboardUpdateTask().getReadWriteLock().readLock().lock();

                String line = reader.readLine();
                while (line != null && !line.isEmpty()) {
                    if (position >= startPos && position <= endPos) {
                        String[] components = line.split(":");
                        //first component is the uuid
                        String playerName = components[1];
                        String levelString = components[2];
                        int level = Integer.parseInt(levelString);

                        results.add(new PlayerStat(playerName, level));
                    }

                    line = reader.readLine();
                    position++;
                }
            } finally {
                plugin.getLeaderboardUpdateTask().getReadWriteLock().readLock().unlock();
            }

            int maxPages = NumberConversions.floor((double) position / 10);
            display(results, maxPages);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error loading leaderboard", ex);
            sender.sendMessage(ChatColor.DARK_RED + "Error loading leaderboard");
        }
    }

    private void display(List<PlayerStat> results, int maxPages) {
        sender.sendMessage(ChatColor.DARK_GREEN + "=== Page " + requestedPage + " / " + maxPages + " ===");

        int rank = 10 * (requestedPage - 1);
        for (PlayerStat result : results) {
            String playerName = result.name;
            int combatLevel = result.statVal;

            sender.sendMessage(ChatColor.GOLD + "" + rank + ". " + playerName + " level: " + combatLevel);
            rank++;
        }
    }
}
