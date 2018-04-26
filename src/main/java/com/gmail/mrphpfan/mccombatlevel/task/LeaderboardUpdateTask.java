package com.gmail.mrphpfan.mccombatlevel.task;

import com.gmail.mrphpfan.mccombatlevel.McCombatLevel;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.bukkit.entity.Player;

public class LeaderboardUpdateTask implements Runnable {

    private final McCombatLevel plugin;

    private final ConcurrentMap<UUID, PlayerStat> toSave = new ConcurrentHashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public LeaderboardUpdateTask(McCombatLevel plugin) {
        this.plugin = plugin;
    }

    public void addToSave(Player player, int level) {
        UUID playerUUID = player.getUniqueId();

        synchronized (this) {
            toSave.put(playerUUID, new PlayerStat(player.getName(), level));
        }
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public void run() {
        if (toSave.isEmpty()) {
            return;
        }

        try {
            Path tempFile = Files.createTempDirectory(plugin.getDataFolder().toPath(), "leaderboard-temp");
            Path originalFile = new File(plugin.getDataFolder(), "leaderboardIndex.txt").toPath();
            if (Files.notExists(originalFile)) {
                Files.createFile(originalFile);
            }

            try (BufferedReader reader = Files.newBufferedReader(originalFile);
                 BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                readWriteLock.writeLock().lock();

                updateExistingEntries(reader, writer);
                appendNewEntries(writer);

                toSave.clear();

                Files.delete(originalFile);
                Files.move(tempFile, originalFile, StandardCopyOption.COPY_ATTRIBUTES);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error saving leaderboard", ex);
        }
    }

    private void updateExistingEntries(BufferedReader reader, BufferedWriter writer) throws IOException {
        String line = reader.readLine();
        while (line != null && !line.isEmpty()) {
            String uuidString = line.substring(0, line.indexOf(':'));
            UUID savedUUID = UUID.fromString(uuidString);

            PlayerStat playerStats = toSave.remove(savedUUID);
            if (playerStats != null) {
                writer.append(uuidString);
                writer.append(':');
                writer.append(playerStats.name);
                writer.append(':');
                writer.append(Integer.toString(playerStats.statVal));
                writer.newLine();
            } else {
                writer.append(line);
            }

            line = reader.readLine();
        }
    }

    private void appendNewEntries(BufferedWriter writer) throws IOException {
        ImmutableMap<UUID, PlayerStat> copyToSave;
        synchronized (this) {
            //require a lock for changes with new add requests
            copyToSave = ImmutableMap.copyOf(toSave);
            toSave.clear();
        }

        for (Map.Entry<UUID, PlayerStat> entry : copyToSave.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerStat value = entry.getValue();

            writer.append(uuid.toString());
            writer.append(':');
            writer.append(value.name);
            writer.append(':');
            writer.append(Integer.toString(value.statVal));
            writer.newLine();
        }
    }
}
