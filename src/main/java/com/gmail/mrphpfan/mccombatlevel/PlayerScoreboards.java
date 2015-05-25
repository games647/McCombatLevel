package com.gmail.mrphpfan.mccombatlevel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerScoreboards {

    //should be unique
    private static final String OBJECTIVE_NAME = "combat_level";

    private Scoreboard board;
    private Objective objective;

    private boolean oldScoreboardAPI;

    public PlayerScoreboards(Scoreboard board, String displayName) {
        this.board = board;

        oldScoreboardAPI = isOldScoreboardAPI();

        //as we use the main scoreboard now we have to clear old values to prevent memory leaks
        removeObjective();

        objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(displayName);
    }

    public void setScore(String playerName, int score) {
        //set the score on the scoreboard
        if (oldScoreboardAPI) {
            objective.getScore(new FastOfflinePlayer(playerName)).setScore(score);
        } else {
            objective.getScore(playerName).setScore(score);
        }
    }

    public void remove(String playerName) {
        if (oldScoreboardAPI) {
            board.resetScores(new FastOfflinePlayer(playerName));
        } else {
            board.resetScores(playerName);
        }
    }

    public void sendAllScoreboard() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            //in order to see the level under the name
            online.setScoreboard(board);
        }
    }

    public void removeObjective() {
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
