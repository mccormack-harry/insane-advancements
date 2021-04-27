package me.hazedev.advancements.api;

import me.hazedev.advancements.api.meta.AdvancementDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InsaneAdvancement extends AbstractAdvancement {

    private final Advancement parent;
    private final String identifier;
    private final int goal;

    public InsaneAdvancement(@NotNull Advancement parent, @NotNull String identifier, int goal, @NotNull AdvancementDisplay display) {
        super(parent.getManager(), new NamespacedKey(parent.getManager(), parent.getRoot().getIdentifier() + "/" + identifier), display);
        this.parent = parent;
        parent.addChild(this);
        this.identifier = identifier;
        this.goal = goal;
    }

    public InsaneAdvancement(@NotNull Advancement parent, @NotNull String identifier, int goal) {
        this(parent, identifier, goal, new AdvancementDisplay());
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
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
