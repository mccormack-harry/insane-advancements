package me.hazedev.advancements.api;

import me.hazedev.advancements.api.meta.AdvancementDisplay;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This implementation represents an advancement tab <br>
 * Note: Any advancement which doesn't have a parent is always displayed in it's own tab as the root
 */
public class RootAdvancement extends AbstractAdvancement {

    private final String identifier;
    private final int goal;

    public RootAdvancement(@NotNull AdvancementManager manager, @NotNull String identifier, int goal, @Nullable AdvancementDisplay display) {
        super(manager, new NamespacedKey(manager, identifier + "/root"), display);
        this.identifier = identifier;
        this.goal = goal;
    }

    public RootAdvancement(@NotNull AdvancementManager manager, @NotNull String identifier, int goal) {
        this(manager, identifier, goal, null);
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @Nullable Advancement getParent() {
        return null;
    }

    @Override
    public int getGoal(@NotNull Player player) {
        return goal;
    }

}
