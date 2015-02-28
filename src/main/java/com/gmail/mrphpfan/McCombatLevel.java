package com.gmail.mrphpfan;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.logging.Level;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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

    private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
    private boolean useEngine = true;

    private Scoreboard board;
    private Objective objective;

    //configuration values
    private boolean enablePrefix = true;
    private boolean enableTag = true;
    private String displayName = ChatColor.GREEN + "Combat";
    private ChatColor prefixBracket = ChatColor.GOLD;
    private ChatColor prefixLevel = ChatColor.DARK_GREEN;
    private String formula = "Math.round((unarmed + swords + axes + archery "
            + "+ .25 * acrobatics + .25 * taming) / 45)";

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
            int swords = getLevel(mcMMOPlayer, SkillType.SWORDS);
            int axes = getLevel(mcMMOPlayer, SkillType.AXES);
            int unarmed = getLevel(mcMMOPlayer, SkillType.UNARMED);
            int archery = getLevel(mcMMOPlayer, SkillType.ARCHERY);
            int taming = getLevel(mcMMOPlayer, SkillType.TAMING);
            int acrobatics = getLevel(mcMMOPlayer, SkillType.ACROBATICS);

            if (useEngine && scriptEngine != null) {
                Bindings variables = scriptEngine.createBindings();
                //create variable for scripting
                variables.put("swords", swords);
                variables.put("axes", axes);
                variables.put("unarmed", unarmed);
                variables.put("archery", archery);
                variables.put("taming", taming);
                variables.put("acrobatics", acrobatics);

                try {
                    Object result = scriptEngine.eval(formula, variables);
                    if (result instanceof Number) {
                        setLevel(player, ((Number) result).intValue());
                    } else {
                        getLogger().warning("Formula doesn't returned a number. Using default forumla now");
                        useEngine = false;
                        updateLevel(player);
                    }
                } catch (ScriptException ex) {
                    getLogger().log(Level.SEVERE, "Combat level cannot be calculated. Using default formula now", ex);
                    useEngine = false;
                    updateLevel(player);
                }
            } else {
                int combatLevel = NumberConversions.round((unarmed + swords + axes + archery
                        + .25 * acrobatics + .25 * taming) / 45);
                setLevel(player, combatLevel);
            }
        }
    }

    private int getLevel(McMMOPlayer mcMMOPlayer, SkillType skillType) {
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
        formula = config.getString("formula");

        if (scriptEngine == null) {
            getLogger().warning("JavaScript Engine not found. Ignoring formula. Please use Java 8");
        }
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
