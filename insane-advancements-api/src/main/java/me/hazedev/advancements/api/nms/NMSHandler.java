package me.hazedev.advancements.api.nms;

import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class NMSHandler {

    protected final AdvancementManager manager;

    public NMSHandler(@NotNull AdvancementManager manager) {
        this.manager = manager;
    }

    public abstract void clearCache(@NotNull UUID uniqueId);

    public abstract void addPacketListener(@NotNull Player player);

    public void addPacketListeners(@NotNull Collection<? extends Player> players) {
        players.forEach(this::addPacketListener);
    }

    public abstract void removePacketListener(@NotNull Player player);

    public void removePacketListeners(@NotNull Collection<? extends Player> players) {
        players.forEach(this::removePacketListener);
    }

    public abstract void sendAdvancementTabs(@NotNull Player player, @NotNull List<Advancement> advancements);

    public abstract void sendAdvancementTab(@NotNull Player player, @NotNull Advancement advancement);

    public abstract void updateProgress(@NotNull Player player, @NotNull Advancement advancement);

    public abstract void sendToasts(@NotNull Player player, @NotNull Advancement... advancements);

    public abstract void clearAdvancements(@NotNull Player player);

}
