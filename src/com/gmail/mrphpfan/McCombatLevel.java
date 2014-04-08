package com.gmail.mrphpfan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

/**
 * @author Mrphpfan
 */
public final class McCombatLevel extends JavaPlugin implements Listener{
	ArrayList<String> allLevels = new ArrayList<String>();
	
	Map<String, Integer> playerLevels = new HashMap<String, Integer>();
	private ScoreboardManager manager;
	private Scoreboard board;
	private Objective objective;
	private boolean enablePrefix = true;
	private boolean enableTag = true;
	private String tagName = "Combat";
	private ChatColor tagColor = ChatColor.GREEN;
	private ChatColor prefixBracketColor = ChatColor.GOLD;
	private ChatColor prefixLevelColor = ChatColor.DARK_GREEN;
	
	
	@Override
    public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		loadConfiguration();
		
		if(enableTag){
			manager = Bukkit.getScoreboardManager();
			board = manager.getNewScoreboard();
			 
			objective = board.registerNewObjective("display", "levels");
			objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
			objective.setDisplayName(tagColor + tagName);
			
			//check if there are any players on yet and set their levels
			for(Player online : Bukkit.getOnlinePlayers()){
				updateLevel(online);
			}
		}
		
		//send the scoreboard initially to online players
		sendScoreboard();
		
		getLogger().info("McCombatLevel Enabled.");
    }
 
    @Override
    public void onDisable() {
    	getLogger().info("McCombatLevel Disabled.");
    }
    
    public void loadConfiguration(){
        File pluginFolder = this.getDataFolder();
		if(!pluginFolder.exists()){
			pluginFolder.mkdir();
		}
		
		FileConfiguration config = this.getConfig();
		
		config.options().copyDefaults(true);
        this.saveConfig();
        
        //enablePrefix
        if(config.get("enable_prefix") == null){
        	config.addDefault("enable_prefix", true);
	        this.saveConfig();
        }
        
        //enableTag
        if(config.get("enable_tag_level") == null){
        	config.addDefault("enable_tag_level", true);
	        this.saveConfig();
        }
        
        //tagName
        if(config.get("tag_name") == null){
        	config.addDefault("tag_name", "Combat");
	        this.saveConfig();
        }
        
        //tagColor
        if(config.get("tag_color") == null){
        	config.addDefault("tag_color", "GREEN");
	        this.saveConfig();
        }
        
        //prefixBracketColor
        if(config.get("prefix_bracket_color") == null){
        	config.addDefault("prefix_bracket_color", "GOLD");
	        this.saveConfig();
        }
        
        //prefixLevelColor
        if(config.get("prefix_level_color") == null){
        	config.addDefault("prefix_level_color", "DARK_GREEN");
	        this.saveConfig();
        }
        
        this.reloadConfig();
        
        //read in values
        Object enableP = config.get("enable_prefix");
        if(enableP != null){
        	enablePrefix = (Boolean) enableP;
        }
        
        Object enableT = config.get("enable_tag_level");
        if(enableT != null){
        	enableTag = (Boolean) enableT;
        }
        
        Object tagN = config.get("tag_name");
        if(tagN != null){
        	tagName = (String) tagN;
        }
        
        Object tagC = config.get("tag_color");
        if(tagC != null){
        	String tagColorStr = (String) tagC;
        	tagColor = ChatColor.valueOf(tagColorStr.toUpperCase());
        }
        
        Object prefixBracketC = config.get("prefix_bracket_color");
        if(prefixBracketC != null){
        	String prefixBracketColorStr = (String) prefixBracketC;
        	prefixBracketColor = ChatColor.valueOf(prefixBracketColorStr.toUpperCase());
        }
        
        Object prefixLevelC = config.get("prefix_level_color");
        if(prefixLevelC != null){
        	String prefixLevelColorStr = (String) prefixLevelC;
        	prefixLevelColor = ChatColor.valueOf(prefixLevelColorStr.toUpperCase());
        }
        
    }
    
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		updateLevel(player);
		
		//send them the scoreboard
		if(enableTag){
			player.setScoreboard(board);
		}
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event){
		Player player = event.getPlayer();
		//remove the player from the hashmap
		playerLevels.remove(player.getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event){
		//check if prefix is enabled
		if(!enablePrefix){
			return;
		}
		CommandSender player = event.getPlayer();
		//append a level prefix to their name
		if(player instanceof Player && playerLevels.get(player.getName()) != null){
			event.setFormat(prefixBracketColor + "[" + prefixLevelColor + playerLevels.get(player.getName()) + prefixBracketColor + "]" + ChatColor.RESET + event.getFormat());
		}
	}
    
    @EventHandler
    public void onPlayerLevelUp(final McMMOPlayerLevelUpEvent event){
        Player player = event.getPlayer();
        SkillType skill = event.getSkill();
        
        //only level up combat if one of the following was leveled
        if(skill.equals(SkillType.SWORDS) || skill.equals(SkillType.ARCHERY)|| skill.equals(SkillType.AXES) || skill.equals(SkillType.UNARMED) ||skill.equals(SkillType.TAMING) ||skill.equals(SkillType.ACROBATICS)){
	        updateLevel(player);
        }
    }
    
    @EventHandler
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		String cmdName = cmd.getName();
		Player player;
		if(sender instanceof Player){
			player = (Player) sender;
		}else{
			sender.sendMessage("You must be a player to execute this command.");
    		return true;
		}
		if(cmdName.equalsIgnoreCase("level") || cmdName.equalsIgnoreCase("combatlevel")){
			int level = playerLevels.get(player.getName());
			player.sendMessage(ChatColor.GOLD + "Combat level: " + ChatColor.DARK_GREEN + level);
		}
		return true;
    }
    
    public void updateLevel(Player player){
    	int swords = ExperienceAPI.getLevel(player, "swords");
        int axes = ExperienceAPI.getLevel(player, "axes");
        int unarmed = ExperienceAPI.getLevel(player, "unarmed");
        int archery = ExperienceAPI.getLevel(player, "archery");
        int taming = ExperienceAPI.getLevel(player, "taming");
        int acrobatics = ExperienceAPI.getLevel(player, "acrobatics");
        
        int relevantSwords = swords <= 1000 ? swords : 1000;
        int relevantAxes = axes <= 1000 ? axes : 1000;
        int relevantUnarmed = unarmed <= 1000 ? unarmed : 1000;
        int relevantArchery = archery <= 1000 ? archery : 1000;
        int relevantTaming = taming <= 1000 ? taming : 1000;
        int relevantAcrobatics = acrobatics <= 1000 ? acrobatics : 1000;
        
        int combatLevel = (int) Math.floor((relevantSwords + relevantAxes + relevantUnarmed + relevantArchery + (.25 * relevantTaming) + (.25 * relevantAcrobatics)) / 45);
        
        setLevel(player, combatLevel);
    }
    
    public void setLevel(Player player, int level){
    	//map the player's name to the level
    	playerLevels.put(player.getName(), level);
        
    	if(enableTag){
	        //set my score on the scoreboard
	        Score score = objective.getScore(player);
			if(playerLevels.get(player.getName()) != null){
				score.setScore(playerLevels.get(player.getName()));
			}
    	}
    }
    
    public void sendScoreboard(){
    	if(enableTag){
			for(Player online : Bukkit.getOnlinePlayers()){
				online.setScoreboard(board);
			}
    	}
    }
    
    public int getCombatLevel(Player player){
    	return playerLevels.get(player.getName());
    }
    
}

