package me.hazedev.advancements.api;

import me.hazedev.advancements.api.advancement.AbstractAdvancement;
import me.hazedev.advancements.api.advancement.Advancement;
import me.hazedev.advancements.api.advancement.meta.AdvancementFrame;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToastNotification extends AbstractAdvancement {

    private static final NamespacedKey KEY = new NamespacedKey("toast", "notification");

    public ToastNotification(@NotNull AdvancementManager manager) {
        super(manager, KEY);
    }

    public ToastNotification(@NotNull AdvancementManager manager, @NotNull AdvancementFrame frame, @NotNull ItemStack icon, @NotNull String title) {
        this(manager);
        setFrame(frame);
        setIcon(icon);
        setTitle(title);
    }

    public ToastNotification(@NotNull Player player, @NotNull Advancement advancement) {
        this(advancement.getManager(), advancement.getFrame(player), advancement.getIcon(player), advancement.getTitle(player));
    }

    @Override
    public @Nullable Advancement getParent() {
        return null;
    }

    @Override
    public int getGoal(@NotNull Player player) {
        return 1;
    }

    @Override
    public boolean isGranted(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean isVisibile(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean doToast(@NotNull Player player) {
        return true;
    }

}
