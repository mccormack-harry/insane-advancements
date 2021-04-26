package me.hazedev.advancements.api.advancement;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InsaneAdvancement extends AbstractAdvancement {

    private final Advancement parent;
    private final int goal;

    public InsaneAdvancement(@NotNull Advancement parent, @NotNull String key, int goal) {
        super(parent.getManager(), new NamespacedKey(parent.getKey().getNamespace(), key));
        this.parent = parent;
        parent.addChild(this);
        this.goal = goal;
    }

    @Override
    public @NotNull Advancement getParent() {
        return this.parent;
    }

    @Override
    public int getGoal(@NotNull Player player) {
        return this.goal;
    }

}
