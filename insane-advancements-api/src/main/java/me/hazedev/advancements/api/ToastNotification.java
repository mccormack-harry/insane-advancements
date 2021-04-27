package me.hazedev.advancements.api;

import me.hazedev.advancements.api.meta.AdvancementDisplay;
import me.hazedev.advancements.api.meta.AdvancementType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ToastNotification extends AbstractAdvancement {

    private static final String NAMESPACE = "toast";

    private static NamespacedKey getRandomKey() {
        return new NamespacedKey(NAMESPACE, UUID.randomUUID().toString());
    }

    public ToastNotification(@NotNull AdvancementManager manager, @NotNull AdvancementDisplay display) {
        super(manager, getRandomKey(), display);
    }

    public ToastNotification(@NotNull AdvancementManager manager, @NotNull AdvancementType frame, @NotNull ItemStack icon, @NotNull String title) {
        this(manager, new AdvancementDisplay().type(frame).icon(icon).title(title));
    }

    @Override
    public @NotNull String getIdentifier() {
        return getKey().getKey();
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
    public boolean isVisible(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean isShowToast(@NotNull Player player) {
        return true;
    }



}
