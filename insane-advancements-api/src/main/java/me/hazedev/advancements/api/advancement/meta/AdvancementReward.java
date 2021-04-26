package me.hazedev.advancements.api.advancement.meta;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface AdvancementReward {

    void grant(@NotNull Player player);

    @NotNull
    static AdvancementReward experience(int experience) {
        return player -> player.giveExp(experience);
    }

    @NotNull
    static AdvancementReward levels(int levels) {
        return player -> player.giveExpLevels(levels);
    }

    @NotNull
    static AdvancementReward message(@NotNull String message) {
        return player -> player.sendMessage(message);
    }

    @NotNull
    static AdvancementReward item(@NotNull ItemStack itemStack) {
        return player -> player.getInventory().addItem(itemStack);
    }

}
