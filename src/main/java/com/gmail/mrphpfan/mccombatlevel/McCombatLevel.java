package com.gmail.mrphpfan.mccombatlevel;

import com.gmail.mrphpfan.mccombatlevel.listener.PlayerListener;
import com.gmail.mrphpfan.mccombatlevel.listener.HeroChatListener;
import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.DefaultCalculator;
import com.gmail.mrphpfan.mccombatlevel.listener.SelfListener;
import com.gmail.mrphpfan.mccombatlevel.npc.NPCListener;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Maps;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public class McCombatLevel extends JavaPlugin {

    //cached combat levels of online players
    //This have to be concurrent because we acess it from a different thread(AsyncChatEvent)
    private final Map<String, Integer> playerLevels = Maps.newConcurrentMap();

    private LevelCalculator levelCalculator;

    private PlayerScoreboards scoreboardManger;
    private Effects effects;
    private NPCListener npcListener;

    //configuration values
    private boolean enablePrefix = true;
    private boolean enableTag = true;
    private String displayName = ChatColor.GREEN + "Combat";
    private ChatColor prefixBracket = ChatColor.GOLD;
    private ChatColor prefixLevel = ChatColor.DARK_GREEN;
    private String levelUpMessage = "You leveled up to a new combat level of: {1}";
    private String broadcastMessage = "";

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

    public PlayerScoreboards getScoreboardManger() {
        return scoreboardManger;
    }

    public Effects getEffects() {
        return effects;
    }

    public String getLevelUpMessage() {
        return levelUpMessage;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public Map<String, Integer> getPlayerLevels() {
        return playerLevels;
    }

    @Override
    public void onEnable() {
        loadConfiguration();

        if (enableTag) {
            //Choose the main scoreboard in order to be compatible with for example ColoredTags
            Scoreboard mainScoreboard = getServer().getScoreboardManager().getMainScoreboard();
            scoreboardManger = new PlayerScoreboards(mainScoreboard, displayName);

            try {
                //check if there are any players on yet and set their levels
                Object onlinePlayersResult = Bukkit.class.getDeclaredMethod("getOnlinePlayers").invoke(null);
                Collection<? extends Player> onlinePlayers;
                if (onlinePlayersResult instanceof Collection<?>) {
                    onlinePlayers = getServer().getOnlinePlayers();
                } else {
                    onlinePlayers = Arrays.asList((Player[]) onlinePlayersResult);
                }

                for (Player online : onlinePlayers) {
                    updateLevel(online);

                    //send the scoreboard initially to online players
                    online.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }

        //register commands
        getCommand("combatlevel").setExecutor(new LevelCommand(this));

        //register listener
        getServer().getPluginManager().registerEvents(new SelfListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        if (getServer().getPluginManager().isPluginEnabled("Herochat")) {
            //support with herochat wouldn't work otherwise
            getServer().getPluginManager().registerEvents(new HeroChatListener(this), this);
        }

        //register citizens/npc integration
        if (getServer().getPluginManager().isPluginEnabled("Citizens") && getConfig().getBoolean("npc.enabled")) {
            npcListener = new NPCListener(this, getConfig().getConfigurationSection("npc"));
            getServer().getPluginManager().registerEvents(npcListener, this);
        }
    }

    @Override
    public void onDisable() {
        //remove our objective, so it won't display if the plugin is deactivated
        if (scoreboardManger != null) {
            scoreboardManger.removeObjective();
        }

        npcListener = null;
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
    }

    public void removeCachedLevels(Player player) {
        final String playerName = player.getName();
        playerLevels.remove(playerName);
        //prevent that objective will be too big
        if (scoreboardManger != null && (npcListener == null || npcListener.existsNPC(playerName))) {
            scoreboardManger.remove(playerName);
        }
    }

    public void updateLevel(Player player) {
        if (UserManager.hasPlayerDataKey(player)) {
            //Check if the player is loaded without exceptions, but with backwards compatibility
            int newLevel = calculateLevel(UserManager.getPlayer(player).getProfile());
            setLevel(player, newLevel);
        }
    }

    public int calculateLevel(PlayerProfile mcMMOPlayer) {
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

        //read in values
        enablePrefix = config.getBoolean("enable_prefix");
        enableTag = config.getBoolean("enable_tag_level");
        levelUpMessage = ChatColor.translateAlternateColorCodes('&', config.getString("levelUpMessage"));
        displayName = ChatColor.translateAlternateColorCodes('&', config.getString("tag_name"));
        broadcastMessage = ChatColor.translateAlternateColorCodes('&', config.getString("broadcastMessage"));
        prefixBracket = ChatColor.valueOf(config.getString("prefix_bracket_color").toUpperCase());
        prefixLevel = ChatColor.valueOf(config.getString("prefix_level_color").toUpperCase());

        //effects
        effects = Effects.create(config.getConfigurationSection("effect"));

        JavaScriptCalculator scriptCalculator = new JavaScriptCalculator(config.getString("formula"));
        if (scriptCalculator.isScriptEnabled()) {
            levelCalculator = scriptCalculator;
        } else {
            getLogger().warning("JavaScript Engine not found. Ignoring formula. Please update to Java 8");
            levelCalculator = new DefaultCalculator();
        }
    }
}
