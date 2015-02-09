package com.gmail.mrphpfan;

import com.gmail.nossr50.api.ExperienceAPI;
import com.google.common.collect.Maps;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.NumberConversions;

/**
 * @author Mrphpfan, games647
 */
public class McCombatLevel extends JavaPlugin {

    private static final String OBJECTIVE_NAME = "display";

    private final Map<String, Integer> playerLevels = Maps.newHashMap();

    private Scoreboard board;
    private Objective objective;

    private boolean enablePrefix = true;
    private boolean enableTag = true;
    private String displayName = "Combat";
    private ChatColor tagColor = ChatColor.GREEN;
    private ChatColor prefixBracketColor = ChatColor.GOLD;
    private ChatColor prefixLevelColor = ChatColor.DARK_GREEN;

    public boolean isTagEnabled() {
        return enableTag;
    }

    public boolean isPrefixEnabled() {
        return enablePrefix;
    }

    public ChatColor getPrefixBracketColor() {
        return prefixBracketColor;
    }

    public ChatColor getPrefixColor() {
        return prefixLevelColor;
    }

    @Override
    public void onEnable() {
        //register listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        loadConfiguration();

        if (enableTag) {
            board = Bukkit.getScoreboardManager().getMainScoreboard();

            removeObjective();

            objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(tagColor + displayName);

            //check if there are any players on yet and set their levels
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateLevel(online);
            }
        }

        //register commands
        getCommand("combatlevel").setExecutor(new LevelCommand(this));

        //send the scoreboard initially to online players
        sendScoreboard();
    }

    @Override
    public void onDisable() {
        removeObjective();
    }

    public void sendScoreboard() {
        if (enableTag) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.setScoreboard(board);
            }
        }
    }

    //can return null if the player isn't in the hashmap
    public Integer getCombatLevel(Player player) {
        return playerLevels.get(player.getName());
    }

    public void setLevel(Player player, int level) {
        //map the player's name to the level
        playerLevels.put(player.getName(), level);

        if (enableTag) {
            //set my score on the scoreboard
            Score score = objective.getScore(player);
            if (playerLevels.get(player.getName()) != null) {
                score.setScore(playerLevels.get(player.getName()));
            }
        }
    }

    public void removeCachedLevels(Player player) {
        playerLevels.remove(player.getName());
    }

    public void updateLevel(Player player) {
        try {
            int swords = calculateScore(player, "swords");
            int axes = calculateScore(player, "axes");
            int unarmed = calculateScore(player, "unarmed");
            int archery = calculateScore(player, "archery");
            int taming = calculateScore(player, "taming");
            int acrobatics = calculateScore(player, "acrobatics");

            int combatLevel = NumberConversions.round((swords + axes + unarmed + archery + (.25 * taming) + (.25 * acrobatics)) / 45);

            setLevel(player, combatLevel);
        } catch (RuntimeException ex) {
            //player not loaded yet
            if (!ex.getClass().getSimpleName().equals("McMMOPlayerNotFoundException")) {
                //don't use it directly it'll end up in a NoClassDefFoundError for older mcMMO versions
                throw ex;
            }
        }
    }

    private int calculateScore(Player player, String skillType) {
        int skillLevel = ExperienceAPI.getLevel(player, skillType);
        return skillLevel <= 1000 ? skillLevel : 1000;
    }

    private void loadConfiguration() {
        //create a config if none exists
        saveDefaultConfig();

        Configuration config = getConfig();
        Configuration defConfig = config.getDefaults();

        boolean changed = false;

        //if a key doesn't exist add it to the config not just as default value
        for (Map.Entry<String, Object> values : defConfig.getValues(true).entrySet()) {
            String key = values.getKey();
            Object value = values.getValue();
            if (!config.isSet(key)) {
                config.set(key, value);
                changed = true;
            }
        }

        if (changed) {
            this.saveConfig();
        }

        //read in values
        enablePrefix = config.getBoolean("enable_prefix");
        enableTag = config.getBoolean("enable_tag_level");
        displayName = config.getString("tag_name");
        tagColor = ChatColor.valueOf(config.getString("tag_color").toUpperCase());
        prefixBracketColor = ChatColor.valueOf(config.getString("prefix_bracket_color").toUpperCase());
        prefixLevelColor = ChatColor.valueOf(config.getString("prefix_level_color").toUpperCase());
    }

    private void removeObjective() {
        objective = board.getObjective("display");
        if (objective != null) {
            //clear old data
            objective.unregister();
        }
    }
}
