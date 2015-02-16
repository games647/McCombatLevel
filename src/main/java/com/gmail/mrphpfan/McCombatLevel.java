package com.gmail.mrphpfan;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Maps;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.NumberConversions;

public class McCombatLevel extends JavaPlugin {

    //should be unique
    private static final String OBJECTIVE_NAME = "combat_level";

    //cached combat levels of online players
    //This have to be concurrent because we acess it from a different thread(AsyncChatEvent)
    private final Map<String, Integer> playerLevels = Maps.newConcurrentMap();

    private Scoreboard board;
    private Objective objective;

    //configuration values
    private boolean enablePrefix = true;
    private boolean enableTag = true;
    private String displayName = ChatColor.GREEN + "Combat";
    private ChatColor prefixBracket = ChatColor.GOLD;
    private ChatColor prefixLevel = ChatColor.DARK_GREEN;

    public boolean isTagEnabled() {
        return enableTag;
    }

    public boolean isPrefixEnabled() {
        return enablePrefix;
    }

    public ChatColor getPrefixBracket() {
        return prefixBracket;
    }

    public ChatColor getPrefixColor() {
        return prefixLevel;
    }

    @Override
    public void onEnable() {
        loadConfiguration();

        if (enableTag) {
            //Choose the main scoreboard in order to be compatible with for example ColoredTags
            board = Bukkit.getScoreboardManager().getMainScoreboard();

            //as we use the main scoreboard now we have to clear old values to prevent memory leaks
            removeObjective();

            objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(displayName);

            //check if there are any players on yet and set their levels
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateLevel(online);
            }
        }

        //send the scoreboard initially to online players
        sendScoreboard();

        //register commands
        getCommand("combatlevel").setExecutor(new LevelCommand(this));

        //register listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        //remove our objective, so it won't display if the plugin is deactivated
        removeObjective();
    }

    public void sendScoreboard() {
        if (enableTag) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                //in order to see the level under the name
                online.setScoreboard(board);
            }
        }
    }

    /**
     * @param player the player
     * @return null if the key doesn't exist
     */
    public Integer getCombatLevel(Player player) {
        return playerLevels.get(player.getName());
    }

    public void setLevel(Player player, int level) {
        final String playerName = player.getName();
        //map the player's name to the level
        playerLevels.put(playerName, level);

        if (enableTag && player.hasPermission("mccombatlevel.showLevelTag")) {
            //set the score on the scoreboard
            objective.getScore(playerName).setScore(level);
        }
    }

    public void removeCachedLevels(Player player) {
        final String playerName = player.getName();
        playerLevels.remove(playerName);
        //prevent that objective will be too big
        board.resetScores(playerName);
    }

    public void updateLevel(Player player) {
        //Check if the player is loaded
        if (UserManager.hasPlayerDataKey(player)) {
            final McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
            int swords = calculateScore(mcMMOPlayer, SkillType.SWORDS);
            int axes = calculateScore(mcMMOPlayer, SkillType.AXES);
            int unarmed = calculateScore(mcMMOPlayer, SkillType.UNARMED);
            int archery = calculateScore(mcMMOPlayer, SkillType.ARCHERY);
            int taming = calculateScore(mcMMOPlayer, SkillType.TAMING);
            int acrobatics = calculateScore(mcMMOPlayer, SkillType.ACROBATICS);

            int combatLevel = NumberConversions.round((swords + axes + unarmed + archery + (.25 * taming) + (.25 * acrobatics)) / 45);

            setLevel(player, combatLevel);
        }
    }

    private int calculateScore(McMMOPlayer mcMMOPlayer, SkillType skillType) {
        int skillLevel = mcMMOPlayer.getSkillLevel(skillType);
        //max of 1000 level
        return skillLevel <= 1000 ? skillLevel : 1000;
    }

    private void loadConfiguration() {
        //create a config if none exists
        saveDefaultConfig();

        Configuration config = getConfig();
        boolean changed = false;

        //if a key doesn't exist add it to the config not just as default value
        for (Map.Entry<String, Object> values : config.getDefaults().getValues(true).entrySet()) {
            String key = values.getKey();
            if (!config.isSet(key)) {
                config.set(key, values.getValue());
                changed = true;
            }
        }

        if (changed) {
            //update the config if changed something
            saveConfig();
        }

        //read in values
        enablePrefix = config.getBoolean("enable_prefix");
        enableTag = config.getBoolean("enable_tag_level");
        displayName = ChatColor.translateAlternateColorCodes('&', config.getString("tag_name"));
        prefixBracket = ChatColor.valueOf(config.getString("prefix_bracket_color").toUpperCase());
        prefixLevel = ChatColor.valueOf(config.getString("prefix_level_color").toUpperCase());
    }

    private void removeObjective() {
        //remove the existed objective if the reference changed
        Objective toRemove = board.getObjective(OBJECTIVE_NAME);
        if (toRemove != null) {
            //clear old data
            toRemove.unregister();
        }
    }
}
