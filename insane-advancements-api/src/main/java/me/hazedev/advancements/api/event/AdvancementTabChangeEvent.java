package me.hazedev.advancements.api.event;

import me.hazedev.advancements.api.Advancement;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancementTabChangeEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final NamespacedKey key;
    private final Advancement tab;

    public AdvancementTabChangeEvent(@NotNull Player player, @NotNull NamespacedKey key, @Nullable Advancement tab) {
        this.player = player;
        this.key = key;
        this.tab = tab;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    @Nullable
    public Advancement getTab() {
        return tab;
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
