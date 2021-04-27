package me.hazedev.advancements.api.event;

import me.hazedev.advancements.api.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AdvancementGrantEvent extends AdvancementProgressEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public AdvancementGrantEvent(@NotNull Player player, @NotNull Advancement advancement) {
        super(player, advancement);
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
