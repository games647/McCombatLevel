package com.gmail.mrphpfan.mccombatlevel;

import com.gmail.mrphpfan.mccombatlevel.calculator.DefaultCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.mrphpfan.mccombatlevel.listener.PlayerListener;
import com.gmail.mrphpfan.mccombatlevel.listener.SelfListener;
import com.gmail.mrphpfan.mccombatlevel.npc.NPCListener;
import com.gmail.mrphpfan.mccombatlevel.tasks.LeaderboardUpdateTask;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Maps;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public class McCombatLevel extends JavaPlugin {

    //cached combat levels of online players
    //This have to be concurrent because we acess it from a different thread(AsyncChatEvent)
    private final Map<String, Integer> playerLevels = Maps.newConcurrentMap();

    private LevelCalculator levelCalculator;

    private CombatScoreboard scoreboardManger;
    private Effects effects;
    private NPCListener npcListener;
    private LeaderboardUpdateTask leaderboardUpdateTask;

    //configuration values
    private boolean enablePrefix = true;
    private boolean enableTag = true;
    private String displayName = ChatColor.GREEN + "Combat";
    private ChatColor prefixBracket = ChatColor.GOLD;
    private ChatColor prefixLevel = ChatColor.DARK_GREEN;
    private String levelUpMessage = "You leveled up to a new combat level of: {1}";
    private String broadcastMessage = "";
    private boolean ranking = false;

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

    public CombatScoreboard getScoreboardManger() {
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

    public LeaderboardUpdateTask getLeaderboardUpdateTask() {
        return leaderboardUpdateTask;
    }

    @Override
    public void onEnable() {
        loadConfiguration();

        if (enableTag) {
            //Choose the main scoreboard in order to be compatible with for example ColoredTags
            Scoreboard mainScoreboard = getServer().getScoreboardManager().getMainScoreboard();
            scoreboardManger = new CombatScoreboard(mainScoreboard, displayName);

            try {
                //check if there are any players on yet and set their levels
                Object onlinePlayersResult = getServer().getClass().getDeclaredMethod("getOnlinePlayers")
                        .invoke(getServer());
                Collection<? extends Player> onlinePlayers;
                if (onlinePlayersResult instanceof Collection<?>) {
                    onlinePlayers = getServer().getOnlinePlayers();
                } else {
                    onlinePlayers = Arrays.asList((Player[]) onlinePlayersResult);
                }

                for (Player online : onlinePlayers) {
                    updateLevel(online);

                    //send the scoreboard initially to online players
                    online.setScoreboard(mainScoreboard);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }

        //register commands
        getCommand("combatlevel").setExecutor(new LevelCommand(this));
        getCommand("ranking").setExecutor(new RankingCommand(this));

        //register listener
        getServer().getPluginManager().registerEvents(new SelfListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        //register citizens/npc integration
        if (getServer().getPluginManager().isPluginEnabled("Citizens") && getConfig().getBoolean("npc.enabled")) {
            npcListener = new NPCListener(this, getConfig().getConfigurationSection("npc"));
            getServer().getPluginManager().registerEvents(npcListener, this);
        }

        if (ranking) {
            leaderboardUpdateTask = new LeaderboardUpdateTask(this);
            //10 minutes
            getServer().getScheduler().runTaskTimerAsynchronously(this, leaderboardUpdateTask, 3 * 20, 10 * 60 * 20);
        }
    }

    @Override
    public void onDisable() {
        if (scoreboardManger != null) {
            scoreboardManger.removeObjective();
        }

        npcListener = null;
        leaderboardUpdateTask = null;
    }

    /**
     * @param player the player
     * @return null if the key doesn't exist
     */
    public Integer getCombatLevel(Player player) {
        return playerLevels.get(player.getName());
    }

    public void setLevel(Player player, int level) {
        String playerName = player.getName();

        // get old level or -1 if player was not loaded
        int oldLevel = playerLevels.containsKey(playerName) ? playerLevels.get(playerName) : -1;
        if (oldLevel != level) {
            PlayerCombatLevelChangeEvent event = new PlayerCombatLevelChangeEvent(player, oldLevel, level);
            getServer().getPluginManager().callEvent(event);
        }
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
        if (mcMMOPlayer != null && levelCalculator != null) {
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

        return -1;
    }

    private void loadConfiguration() {
        saveDefaultConfig();

        //read in values
        enablePrefix = getConfig().getBoolean("enable_prefix");
        enableTag = getConfig().getBoolean("enable_tag_level");
        ranking = getConfig().getBoolean("ranking");
        levelUpMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("levelUpMessage"));
        displayName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("tag_name"));
        broadcastMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("broadcastMessage"));
        prefixBracket = ChatColor.valueOf(getConfig().getString("prefix_bracket_color").toUpperCase());
        prefixLevel = ChatColor.valueOf(getConfig().getString("prefix_level_color").toUpperCase());

        //effects
        effects = Effects.create(getConfig().getConfigurationSection("effect"));

        JavaScriptCalculator scriptCalculator = new JavaScriptCalculator(getConfig().getString("formula"));
        if (scriptCalculator.isScriptEnabled()) {
            levelCalculator = scriptCalculator;
        } else {
            getLogger().warning("JavaScript Engine not found. Ignoring formula. Please update to Java 8");
            levelCalculator = new DefaultCalculator();
        }
    }
}
