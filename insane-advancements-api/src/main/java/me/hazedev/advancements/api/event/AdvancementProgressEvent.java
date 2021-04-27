package me.hazedev.advancements.api.event;

import me.hazedev.advancements.api.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AdvancementProgressEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Advancement advancement;

    public AdvancementProgressEvent(@NotNull Player player, @NotNull Advancement advancement) {
        this.player = player;
        this.advancement = advancement;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Advancement getAdvancement() {
        return advancement;
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
