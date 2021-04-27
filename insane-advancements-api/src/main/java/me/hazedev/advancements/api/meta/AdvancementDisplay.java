package me.hazedev.advancements.api.meta;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This class stores all display information about an advancement and is easily configured
 */
public class AdvancementDisplay {

    private @NotNull AdvancementVisibility visibility = AdvancementVisibility.VANILLA;
    private float x, y = 0;
    private @NotNull String title = "Title";
    private @NotNull String description = "Description";
    private @NotNull ItemStack icon = new ItemStack(Material.STONE);
    private @NotNull AdvancementType type = AdvancementType.TASK;
    private boolean showToast, announce = true;
    private NamespacedKey background = NamespacedKey.minecraft("textures/gui/advancements/backgrounds/stone.png");

    public AdvancementDisplay() {}

    public @NotNull AdvancementDisplay visibility(@NotNull AdvancementVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public @NotNull AdvancementDisplay x(float x) {
        this.x = x;
        return this;
    }

    public @NotNull AdvancementDisplay y(float y) {
        this.y = y;
        return this;
    }

    public @NotNull AdvancementDisplay pos(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public @NotNull AdvancementDisplay title(@NotNull String title) {
        this.title = title;
        return this;
    }

    public @NotNull AdvancementDisplay desc(@NotNull String description) {
        this.description = description;
        return this;
    }

    public @NotNull AdvancementDisplay icon(@NotNull Material material) {
        this.icon = new ItemStack(material);
        return this;
    }

    public @NotNull AdvancementDisplay icon(@NotNull ItemStack itemStack) {
        this.icon = itemStack;
        return this;
    }

    public @NotNull AdvancementDisplay type(@NotNull AdvancementType type) {
        this.type = type;
        return this;
    }

    public @NotNull AdvancementDisplay toast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    public @NotNull AdvancementDisplay announce(boolean announce) {
        this.announce = announce;
        return this;
    }

    public @NotNull AdvancementDisplay background(@NotNull NamespacedKey key) {
        this.background = key;
        return this;
    }

    public @NotNull AdvancementDisplay background(@NotNull String background) {
        this.background = NamespacedKey.minecraft(background);
        return this;
    }

    public @NotNull AdvancementDisplay background(@NotNull Material block) {
        if (block.isBlock()) {
            this.background = NamespacedKey.minecraft("textures/block/" + block.name().toLowerCase(Locale.ROOT) + ".png");
        }
        return this;
    }

    /**
     * Default: {@link AdvancementVisibility#VANILLA}
     * @return The advancement visibility
     */
    public @NotNull AdvancementVisibility getVisibility() {
        return visibility;
    }

    /**
     * Default: 0
     * @return X coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Default: 0
     * @return Y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Default: "Title"
     * @return The title
     */
    public @NotNull String getTitle() {
        return title;
    }


    /**
     * Default: "Description"
     * @return The description
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * Default: {@link Material#STONE}
     * @return The icon
     */
    public @NotNull ItemStack getIcon() {
        return icon;
    }

    /**
     * Default: {@link AdvancementType#TASK}
     * @return The advancement type
     */
    public @NotNull AdvancementType getType() {
        return type;
    }

    /**
     * Default: true
     * @return Whether this advancement should show a toast notification when it's granted
     */
    public boolean isShowToast() {
        return showToast;
    }


    /**
     * Default: true
     * @return Whether this advancement should make an announcement when it's granted
     */
    public boolean isAnnounce() {
        return announce;
    }

    /**
     * Default: "minecraft:textures/gui/advancements/backgrounds/stone.png"
     * @return The tab background texture
     */
    @NotNull
    public NamespacedKey getBackground() {
        return background;
    }
}
