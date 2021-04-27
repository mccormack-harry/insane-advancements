package me.hazedev.advancements.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.hazedev.advancements.api.meta.AdvancementType;
import me.hazedev.advancements.api.event.AdvancementGrantEvent;
import me.hazedev.advancements.api.event.AdvancementProgressEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Advancement {

    /**
     * @return The advancement manager
     */
    @NotNull AdvancementManager getManager();

    /**
     * @return The permanent unique identifier for this advancement, in the format 'insaneadvancements:root_identifier/this_identifier'
     */
    @NotNull NamespacedKey getKey();

    /**
     * @return The internal name of this advancement
     */
    @NotNull String getIdentifier();

    /**
     * Saves all progress
     * @param saveChildren Whether to also save the children advancements
     */
    void saveProgress(boolean saveChildren);

    /**
     * Loads the progress of the players
     * @param players The progress of these players will be loaded
     * @param loadChildren Whether to also load progress for children advancements
     */
    void loadProgress(@NotNull Collection<? extends Player> players, boolean loadChildren);

    /**
     * Loads the progress of a single player
     * @param player The progress of this player will be loaded
     * @param loadChildren Whether to also load progress for children advancements
     */
    default void loadProgress(@NotNull Player player, boolean loadChildren) {
        loadProgress(Collections.singleton(player), loadChildren);
    }

    /**
     * @return The advancement tab which contains this advancement
     */
    @NotNull Advancement getRoot();

    /**
     * @return The direct parent for this advancement
     */
    @Nullable Advancement getParent();


    /**
     * @param child A child of this advancement, otherwise neglected
     */
    void addChild(@NotNull Advancement child);

    /**
     * @return This advancements children
     */
    @NotNull List<Advancement> getChildren();

    /**
     * @return The player's goal
     */
    int getGoal(@NotNull Player player);

    /**
     * @return The player's Progress
     */
    int getProgress(@NotNull Player player);

    /**
     * @return Whether this advancement has been granted
     */
    default boolean isGranted(@NotNull Player player) {
        return getGrantedTime(player) > 0;
    }

    /**
     * @return The epoch milli at which the player was granted the advancement, or 0 if it hasn't been granted
     */
    long getGrantedTime(@NotNull Player player);

    /**
     * Grants the rewards to the player and calls {@link AdvancementGrantEvent}
     */
    void grant(@NotNull Player player);

    /**
     * Calling this method will trigger a {@link AdvancementProgressEvent}
     * @param player The player who progressed
     * @param amount The amount of progress they made
     */
    void addProgress(@NotNull Player player, int amount);

    /**
     * @return Whether this advancement is visible to the player
     */
    boolean isVisible(@NotNull Player player);

    /**
     * @return The X position of this advancement for the player
     */
    float getX(@NotNull Player player);

    /**
     * @return The Y position of this advancement for the player
     */
    float getY(@NotNull Player player);

    /**
     * @return The raw title to display to the player
     */
    @NotNull String getTitle(@NotNull Player player);

    /**
     * @return The title to display to the player
     */
    @NotNull
    default JsonElement getJsonTitle(@NotNull Player player) {
        JsonObject title = new JsonObject();
        title.addProperty("text", getTitle(player));
        return title;
    }

    /**
     * @return The raw description to display to the player
     */
    @NotNull String getDescription(@NotNull Player player);

    /**
     * @return The description to display to the player
     */
    @NotNull
    default JsonElement getJsonDescription(@NotNull Player player) {
        JsonObject description = new JsonObject();
        description.addProperty("text", getDescription(player));
        return description;
    }

    /**
     * @return The icon to display to the player
     */
    @NotNull ItemStack getIcon(@NotNull Player player);

    /**
     * @return The frame to display to the player
     */
    @NotNull AdvancementType getType(@NotNull Player player);

    /**
     * @return Whether a toast notification should be shown to the player after being granted this advancement
     */
    default boolean isShowToast(@NotNull Player player) {
        return true;
    }

    /**
     * @return Whether an announcement should be displayed in chat when the player is granted this advancement
     */
    default boolean isAnnounce(@NotNull Player player) {
        return true;
    }

    /**
     * This method is only relevant in advancements which don't have a parent (root advancements)
     * @return The background displayed in this advancement tab
     */
    @Nullable
    NamespacedKey getBackground(@NotNull Player player);

}
