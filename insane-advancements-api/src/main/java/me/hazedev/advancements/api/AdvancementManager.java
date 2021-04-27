package me.hazedev.advancements.api;

import me.hazedev.advancements.api.nms.NMSHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class AdvancementManager extends JavaPlugin {

    protected static AdvancementManager instance = null;

    /**
     * To get an instance of this class use {@link AdvancementManager#getInstance()}
     */
    public AdvancementManager() throws UnsupportedOperationException {
        if (instance != null) throw new UnsupportedOperationException("You cannot instantiate the advancement manager directly");
    }

    /**
     * @return Gets an instance of the advancement manager, or null if the plugin is not installed and enabled
     */
    @Nullable
    public static AdvancementManager getInstance() {
        return instance;
    }

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
