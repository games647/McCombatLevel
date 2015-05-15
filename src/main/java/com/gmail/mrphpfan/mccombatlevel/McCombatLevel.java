package com.gmail.mrphpfan.mccombatlevel;

import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.DefaultCalculator;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class McCombatLevel extends JavaPlugin {

    //should be unique
    private static final String OBJECTIVE_NAME = "combat_level";

    //cached combat levels of online players
    //This have to be concurrent because we acess it from a different thread(AsyncChatEvent)
    private final Map<String, Integer> playerLevels = Maps.newConcurrentMap();

    private LevelCalculator levelCalculator;
    private boolean oldScoreboardAPI;

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

    public void setLevelCalculator(LevelCalculator newCalculator) {
        levelCalculator = newCalculator;
    }

    @Override
    public void onEnable() {
        loadConfiguration();

        if (enableTag) {
            oldScoreboardAPI = isOldScoreboardAPI();

            //Choose the main scoreboard in order to be compatible with for example ColoredTags
            board = getServer().getScoreboardManager().getMainScoreboard();

            //as we use the main scoreboard now we have to clear old values to prevent memory leaks
            removeObjective();

            objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            objective.setDisplayName(displayName);

            //check if there are any players on yet and set their levels
            for (Player online : getServer().getOnlinePlayers()) {
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
            for (Player online : getServer().getOnlinePlayers()) {
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
        
        // get old level or -1 if player was not loaded
        int oldLevel = playerLevels.containsKey(playerName) ? playerLevels.get(playerName) : -1;
        if (oldLevel == level) {
        	// do nothing if old level == new level
        	return;
        }
        
        // create and call event
        PlayerCombatLevelChangeEvent event = new PlayerCombatLevelChangeEvent(player, oldLevel, level);
        getServer().getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            //map the player's name to the level
            playerLevels.put(playerName, event.getNewLevel());

            if (enableTag && player.hasPermission(getName().toLowerCase() + ".showLevelTag")) {
                //set the score on the scoreboard
                if (oldScoreboardAPI) {
                    objective.getScore(new FastOfflinePlayer(playerName)).setScore(level);
                } else {
                    objective.getScore(playerName).setScore(level);
                }
            }
        }
    }

    public void removeCachedLevels(Player player) {
        final String playerName = player.getName();
        playerLevels.remove(playerName);
        //prevent that objective will be too big
        if (enableTag) {
            if (oldScoreboardAPI) {
                board.resetScores(new FastOfflinePlayer(playerName));
            } else {
                board.resetScores(playerName);
            }
        }
    }

    public void updateLevel(Player player) {
        if (UserManager.hasPlayerDataKey(player)) {
            //Check if the player is loaded without exceptions, but with backwards compatibility
            int newLevel = calculateLevel(UserManager.getPlayer(player));
            setLevel(player, newLevel);
        }
    }

    public int calculateLevel(McMMOPlayer mcMMOPlayer) {
        if (mcMMOPlayer == null || levelCalculator == null) {
            return -1;
        }

        try {
            return levelCalculator.calculateLevel(mcMMOPlayer);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Exception occured falling back", ex);
            if (levelCalculator instanceof DefaultCalculator) {
                //nulling the calculator, because event the default replacer created an exception
                levelCalculator = null;
            }

            levelCalculator = new DefaultCalculator();
            return calculateLevel(mcMMOPlayer);
        }
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

        JavaScriptCalculator scriptCalculator = new JavaScriptCalculator(config.getString("formula"));
        if (scriptCalculator.isScriptEnabled()) {
            levelCalculator = scriptCalculator;
        } else {
            getLogger().warning("JavaScript Engine not found. Ignoring formula. Please update to Java 8"
                    + " https://www.java.com/download/");
            levelCalculator = new DefaultCalculator();
        }
    }

    private void removeObjective() {
        //remove the existed objective if the reference changed
        if (board != null) {
            Objective toRemove = board.getObjective(OBJECTIVE_NAME);
            if (toRemove != null) {
                //clear all old data
                toRemove.unregister();
            }
        }
    }

    private boolean isOldScoreboardAPI() {
        try {
            Objective.class.getDeclaredMethod("getScore", String.class);
        } catch (NoSuchMethodException noSuchMethodEx) {
            //since we have an extra class for it (FastOfflinePlayer)
            //we can fail silently
            return true;
        }

        //We have access to the new method
        return false;
    }
}
