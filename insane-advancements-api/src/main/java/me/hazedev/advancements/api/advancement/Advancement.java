package me.hazedev.advancements.api.advancement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.advancement.meta.AdvancementFrame;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface Advancement {

    @NotNull AdvancementManager getManager();

    @NotNull NamespacedKey getKey();

    void saveProgress(@NotNull UUID uniqueId);

    void loadProgress(@NotNull UUID uniqueId);

    default void saveProgress(@NotNull Player player) {
        saveProgress(player.getUniqueId());
        getChildren().forEach(advancement -> advancement.saveProgress(player));
    }

    default void loadProgress(@NotNull Player player) {
        loadProgress(player.getUniqueId());
        getChildren().forEach(advancement -> advancement.loadProgress(player));
    }

    @NotNull Advancement getRoot();

    @Nullable Advancement getParent();

    void addChild(@NotNull Advancement child);

    @NotNull List<Advancement> getChildren();

    int getGoal(@NotNull Player player);

    int getProgress(@NotNull Player player);

    default boolean isGranted(@NotNull Player player) {
        return getGrantedTime(player) > 0;
    }

    /**
     * @param player The player
     * @return The epoch milli at which the player was granted the advancement, or 0 if they haven't been granted it
     */
    long getGrantedTime(@NotNull Player player);

    void grant(@NotNull Player player);

    void addProgress(@NotNull Player player, int amount);

    boolean isVisibile(@NotNull Player player);

    @NotNull String getTitle(@NotNull Player player);

    @NotNull
    default JsonElement getJsonTitle(@NotNull Player player) {
        JsonObject title = new JsonObject();
        title.addProperty("text", getTitle(player));
        return title;
    }

    @NotNull String getDescription(@NotNull Player player);

    @NotNull
    default JsonElement getJsonDescription(@NotNull Player player) {
        JsonObject description = new JsonObject();
        description.addProperty("text", getDescription(player));
        return description;
    }

    @NotNull ItemStack getIcon(@NotNull Player player);

    @NotNull AdvancementFrame getFrame(@NotNull Player player);

    default boolean doToast(@NotNull Player player) {
        return true;
    }

    default boolean doAnnouncement(@NotNull Player player) {
        return true;
    }

    @NotNull
    default String getBackground(@NotNull Player player) {
        return "textures/blocks/stone.png";
    }

}
