package me.hazedev.advancements.api;

import me.hazedev.advancements.api.advancement.Advancement;
import me.hazedev.advancements.api.nms.NMSHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class AdvancementManager extends JavaPlugin {

    public abstract void saveAll();

    @NotNull
    public abstract File getProgressDir();

    public abstract NMSHandler getNmsHandler();

    public abstract void registerAdvancementTabs(@NotNull List<Advancement> advancements);

    public abstract void registerAdvancementTab(@NotNull Advancement... advancements);

    @NotNull
    public abstract List<Advancement> getTabs();

    @Nullable
    public abstract Advancement getAdvancementTab(@NotNull NamespacedKey key);

}
