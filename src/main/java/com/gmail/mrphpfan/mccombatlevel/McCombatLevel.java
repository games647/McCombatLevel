package com.gmail.mrphpfan.mccombatlevel;

import com.gmail.mrphpfan.mccombatlevel.calculator.DefaultCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.JavaScriptCalculator;
import com.gmail.mrphpfan.mccombatlevel.calculator.LevelCalculator;
import com.gmail.mrphpfan.mccombatlevel.listener.PlayerListener;
import com.gmail.mrphpfan.mccombatlevel.listener.SelfListener;
import com.gmail.mrphpfan.mccombatlevel.npc.NPCListener;
import com.gmail.mrphpfan.mccombatlevel.task.LeaderboardUpdateTask;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.util.player.UserManager;

import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

public class McCombatLevel extends JavaPlugin {

    //cached combat levels of online players
    //This have to be concurrent because we access it from a different thread(AsyncChatEvent)
    private final Map<String, Integer> playerLevels = new ConcurrentHashMap<>();

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
    private boolean ranking;

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

            //check if there are any players on yet and set their levels
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateLevel(online);

                //send the scoreboard initially to online players
                online.setScoreboard(mainScoreboard);
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

        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "combat_level", replaceEvent -> {
                if (!replaceEvent.isOnline()) {
                    return "Player not online";
                }

                Player player = replaceEvent.getPlayer();
                OptionalInt combatLevel = getLevel(player);
                if (combatLevel.isPresent()) {
                    return Integer.toString(combatLevel.getAsInt());
                }

                return "Level not loaded";
            });
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

    public OptionalInt getLevel(Player player) {
        Integer level = playerLevels.get(player.getName());
        if (level == null) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(level);
    }

    @Deprecated
    public Integer getCombatLevel(Player player) {
        return getLevel(player).orElse(0);
    }

    public void setLevel(Player player, int level) {
        // get old level or -1 if player was not loaded
        int oldLevel = playerLevels.getOrDefault(player, -1);
        if (oldLevel != level) {
            PlayerCombatLevelChangeEvent event = new PlayerCombatLevelChangeEvent(player, oldLevel, level);
            getServer().getPluginManager().callEvent(event);
        }
    }

    public void removeCachedLevels(Player player) {
        String playerName = player.getName();
        playerLevels.remove(playerName);
        //prevent that objective will be too big
        scoreboardManger.remove(playerName);
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
            getLogger().warning("JavaScript Engine not found. Ignoring formula...");
            levelCalculator = new DefaultCalculator();
        }
    }
}
