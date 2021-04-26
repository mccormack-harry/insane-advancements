package me.hazedev.advancements.api.event;

import me.hazedev.advancements.api.AdvancementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AdvancementScreenCloseEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final AdvancementManager manager;
    private final Player player;

    public AdvancementScreenCloseEvent(@NotNull AdvancementManager manager, @NotNull Player player) {
        this.manager = manager;
        this.player = player;
    }

    @NotNull
    public AdvancementManager getManager() {
        return manager;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
