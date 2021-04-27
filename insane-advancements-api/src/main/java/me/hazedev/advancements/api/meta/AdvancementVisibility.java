package me.hazedev.advancements.api.meta;

import me.hazedev.advancements.api.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This functional interface has some preset visibilities which you can use to specify how your advancements are revealed.
 * NOTE: If an advancement is invisible, all children are inherently invisible.
 */
@FunctionalInterface
public interface AdvancementVisibility {

    /**
     * @return Whether the advancement is visible to the player
     */
    boolean isVisible(@NotNull Advancement advancement, @NotNull Player player);

    /**
     * The advancement is always visible
     */
    AdvancementVisibility ALWAYS = (advancement, player) -> true;

    /**
     * The advancement is visible if it has been granted
     */
    AdvancementVisibility GRANTED = Advancement::isGranted;

    /**
     * The advancement is visible if either it or it's parent is granted
     */
    AdvancementVisibility PARENT_GRANTED = (advancement, player) -> {
        if (advancement.isGranted(player)) return true;
        Advancement parent = advancement.getParent();
        return parent != null && parent.isGranted(player);
    };

    /**
     * The advancement is visible if either it, it's parent or it's grand parent is granted
     */
    AdvancementVisibility GRAND_PARENT_GRANTED = (advancement, player) -> {
        if (advancement.isGranted(player)) return true;
        Advancement parent = advancement.getParent();
        if (parent != null) {
            if (parent.isGranted(player)) return true;
            Advancement grandParent = parent.getParent();
            return grandParent != null && grandParent.isGranted(player);
        }
        return false;
    };

    /**
     * The advancement is visible if it has been granted or if the root has been granted
     */
    AdvancementVisibility ROOT_GRANTED = (advancement, player) -> advancement.isGranted(player) || advancement.getRoot().isGranted(player);

    /**
     * The advancement is visible if it has been granted or if the root is visible
     */
    AdvancementVisibility ROOT_VISIBLE = (advancement, player) -> {
        if (advancement.isGranted(player)) return true;
        Advancement root = advancement.getRoot();
        return advancement != root && advancement.getRoot().isVisible(player);
    };

    /**
     * The advancement is visible if it has been granted or if any child of this advancement is granted
     */
    AdvancementVisibility CHILD_GRANTED = new AdvancementVisibility() {
        @Override
        public boolean isVisible(@NotNull Advancement advancement, @NotNull Player player) {
            if (advancement.isGranted(player)) return true;
            for (Advancement child : advancement.getChildren()) {
                if (this.isVisible(child, player)) return true;
            }
            return false;
        }
    };

    /**
     * The advancement is visible if it has been granted or if a direct child of this advancement is granted
     */
    AdvancementVisibility DIRECT_CHILD_GRANTED = (advancement, player) -> {
        if (advancement.isGranted(player)) return true;
        for (Advancement child: advancement.getChildren()) {
            if (child.isGranted(player)) return true;
        }
        return false;
    };

    /**
     * The vanilla visibility behaviour: A combination of {@link #GRAND_PARENT_GRANTED} {@link #or} {@link #CHILD_GRANTED}
     */
    AdvancementVisibility VANILLA = GRAND_PARENT_GRANTED.or(CHILD_GRANTED);

    /**
     * The advancement is never visible
     */
    AdvancementVisibility HIDDEN = (advancement, player) -> false;

    /**
     * This method allows you to combine another visibility with an AND operator
     *
     * @param visibility The visibility to combine
     * @return The combined visibility
     */
    default @NotNull AdvancementVisibility and(@NotNull AdvancementVisibility visibility) {
        return (advancement, player) -> this.isVisible(advancement, player) && visibility.isVisible(advancement, player);
    }

    /**
     * This method allows you to combine another visibility with an OR operator
     *
     * @param visibility The visibility to combine
     * @return The combined visibility
     */
    default @NotNull AdvancementVisibility or(@NotNull AdvancementVisibility visibility) {
        return (advancement, player) -> this.isVisible(advancement, player) || visibility.isVisible(advancement, player);
    }

    /**
     * This method allows you to invert a visibility to return the opposite
     *
     * @param visibility The visibility to invert
     * @return The inverted visibility
     */
    static @NotNull AdvancementVisibility not(@NotNull AdvancementVisibility visibility) {
        return (advancement, player) -> !visibility.isVisible(advancement, player);
    }

}
