package com.gmail.mrphpfan.mccombatlevel;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a players combat level changes.
 *
 * So also when the mcMMO profile is loaded. The old value will be -1 then.
 *
 * @author Zettelkasten
 */
public class PlayerCombatLevelChangeEvent extends PlayerEvent implements Cancellable {

    private final static HandlerList handlers = new HandlerList();

    private final int oldLevel;
    private int newLevel;

    private boolean cancel;

    public PlayerCombatLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        super(player);

        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /**
     * Returns the level the player had previously.
     *
     * @return the previous level or <code>-1</code> if the player was not loaded before.
     */
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * Returns the new level of the player
     *
     * @return the new level of the player
     */
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * Changes the new level of the player
     *
     * @param newLevel the new level of the player
     */
    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
