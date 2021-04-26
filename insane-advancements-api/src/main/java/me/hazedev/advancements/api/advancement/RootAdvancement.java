package me.hazedev.advancements.api.advancement;

import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.advancement.meta.AdvancementVisibility;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RootAdvancement extends AbstractAdvancement {

    String background = "";

    public RootAdvancement(@NotNull AdvancementManager manager, @NotNull String namespace, @NotNull String key) {
        super(manager, new NamespacedKey(namespace, key));
        setBackground(Material.STONE);
        visibility = AdvancementVisibility.HIDDEN;
    }

    @Override
    public @Nullable Advancement getParent() {
        return null;
    }

    @Override
    public int getGoal(@NotNull Player player) {
        return 0;
    }

    @Override
    public @NotNull String getBackground(@NotNull Player player) {
        return this.background;
    }

    @NotNull
    public RootAdvancement setBackground(@NotNull String background) {
        this.background = background;
        return this;
    }

    @NotNull
    public RootAdvancement setBackground(@NotNull Material background) {
        this.background = "textures/block/" + background.name().toLowerCase() + ".png";
        return this;
    }

}
