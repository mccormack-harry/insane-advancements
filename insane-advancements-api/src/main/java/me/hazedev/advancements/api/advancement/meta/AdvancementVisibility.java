package me.hazedev.advancements.api.advancement.meta;

import me.hazedev.advancements.api.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface AdvancementVisibility {

    boolean isVisible(@NotNull Advancement advancement, @NotNull Player player);

    AdvancementVisibility ALWAYS = (advancement, player) -> true;
    AdvancementVisibility PARENT_GRANTED = (advancement, player) -> {
        Advancement parent = advancement.getParent();
        return advancement.isGranted(player) || parent == null || parent.isGranted(player);
    };
    AdvancementVisibility VANILLA = (advancement, player) -> {
        if (advancement.isGranted(player)) return true;

        Advancement parent = advancement.getParent();
        if (parent != null && !parent.isGranted(player)) {
            Advancement grandParent = parent.getParent();
            return grandParent == null || grandParent.getParent() == null || grandParent.isGranted(player);
        }
        return true;
    };
    AdvancementVisibility HIDDEN = Advancement::isGranted;
    
}
