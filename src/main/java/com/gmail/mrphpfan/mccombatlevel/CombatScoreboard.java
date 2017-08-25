package com.gmail.mrphpfan.mccombatlevel;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class CombatScoreboard {

    //should be unique
    private static final String OBJECTIVE_NAME = "combat_level";

    private final Scoreboard board;
    private final Objective objective;

    public CombatScoreboard(Scoreboard board, String displayName) {
        this.board = board;

        //as we use the main scoreboard now we have to clear old values to prevent memory leaks
        removeObjective();

        objective = board.registerNewObjective(OBJECTIVE_NAME, "dummy");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(displayName);
    }

    public void setScore(String playerName, int score) {
        objective.getScore(playerName).setScore(score);
    }

    public void remove(String playerName) {
        board.resetScores(playerName);
    }

    public void removeObjective() {
        //remove our objective, so it won't display if the plugin is deactivated
        if (board != null) {
            Objective toRemove = board.getObjective(OBJECTIVE_NAME);
            if (toRemove != null) {
                //clear all old data
                toRemove.unregister();
            }
        }
    }
}
