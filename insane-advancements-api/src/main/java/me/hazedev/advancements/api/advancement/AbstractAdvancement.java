package me.hazedev.advancements.api.advancement;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.advancement.meta.AdvancementFrame;
import me.hazedev.advancements.api.advancement.meta.AdvancementReward;
import me.hazedev.advancements.api.advancement.meta.AdvancementVisibility;
import me.hazedev.advancements.api.event.AdvancementGrantEvent;
import me.hazedev.advancements.api.event.AdvancementProgressEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractAdvancement implements Advancement {

    protected final AdvancementManager manager;

    protected final NamespacedKey key;
    protected final List<Advancement> children = new ArrayList<>();
    protected final List<AdvancementReward> rewards = new ArrayList<>();
    protected final Map<UUID, Long> grantedMap = new HashMap<>();
    protected final Map<UUID, Integer> progressMap = new HashMap<>();

    protected AdvancementVisibility visibility = AdvancementVisibility.PARENT_GRANTED;
    protected String title = "Advancement";
    protected String description = "Description";
    protected ItemStack icon = new ItemStack(Material.STONE);
    protected AdvancementFrame frame = AdvancementFrame.TASK;
    protected boolean showToast = true;
    protected boolean announce = true;

    public AbstractAdvancement(@NotNull AdvancementManager manager, @NotNull NamespacedKey key) {
        this.manager = manager;
        this.key = key;
    }

    @Override
    public @NotNull AdvancementManager getManager() {
        return manager;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @NotNull
    public File getProgressFile(@NotNull UUID uniqueId) {
        final String fileName = getKey().getNamespace()
                + File.separatorChar + getKey().getKey()
                + File.separatorChar + uniqueId;
        return new File(getManager().getProgressDir(), fileName + ".json");
    }

    @Override
    public void saveProgress(@NotNull UUID uniqueId) {
        JsonObject json = new JsonObject();
        long granted = grantedMap.getOrDefault(uniqueId, 0L);
        if (granted > 0) {
            json.addProperty("granted", granted);
        } else {
            int progress = progressMap.getOrDefault(uniqueId, 0);
            if (progress > 0) {
                json.addProperty("progress", progress);
            }
        }
        if (json.size() > 0) {
            File file = getProgressFile(uniqueId);
            file.getParentFile().mkdirs();
            try (FileWriter fileWriter = new FileWriter(file)) {
                new Gson().toJson(json, fileWriter);
            } catch (IOException e) {
                getManager().getLogger().severe("Failed to save progress for advancement " + getKey() + " - " + uniqueId);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loadProgress(@NotNull UUID uniqueId) {
        File progressFile = getProgressFile(uniqueId);
        if (progressFile.exists()) {
            JsonObject json;
            try (BufferedReader fileReader = new BufferedReader(new FileReader(progressFile))) {
                json = (JsonObject) new JsonParser().parse(fileReader);
            } catch (IOException e) {
                getManager().getLogger().severe("Failed to load progress for advancement " + getKey() + " - " + uniqueId);
                e.printStackTrace();
                return;
            }
            JsonElement granted = json.get("granted");
            if (granted != null && granted.isJsonPrimitive()) {
                grantedMap.put(uniqueId, granted.getAsLong());
            }
            JsonElement progress = json.get("progress");
            if (progress != null && progress.isJsonPrimitive()) {
                progressMap.put(uniqueId, progress.getAsInt());
            }
        }
    }

    @Override
    public @NotNull Advancement getRoot() {
        Advancement root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    @Override
    public void addChild(@NotNull Advancement child) {
        this.children.add(child);
    }

    @Override
    public @NotNull List<Advancement> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public int getProgress(@NotNull Player player) {
        return isGranted(player) ? getGoal(player) : progressMap.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public long getGrantedTime(@NotNull Player player) {
        return grantedMap.getOrDefault(player.getUniqueId(), 0L);
    }

    @Override
    public void grant(@NotNull Player player) {
        grantedMap.put(player.getUniqueId(), System.currentTimeMillis());
        for (AdvancementReward reward: rewards) {
            reward.grant(player);
        }
        Bukkit.getPluginManager().callEvent(new AdvancementGrantEvent(player, this));
    }

    @Override
    public void addProgress(@NotNull Player player, int amount) {
        if (!isGranted(player)) {
            if (progressMap.containsKey(player.getUniqueId())) {
                progressMap.merge(player.getUniqueId(), amount, Integer::sum);
            } else {
                progressMap.put(player.getUniqueId(), amount);
            }
            if (getProgress(player) >= getGoal(player)) {
                grant(player);
            }
            Bukkit.getPluginManager().callEvent(new AdvancementProgressEvent(player, this));
        }
    }

    @Override
    public boolean isVisibile(@NotNull Player player) {
        return visibility.isVisible(this, player);
    }

    @NotNull
    public AbstractAdvancement setVisibility(AdvancementVisibility visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public @NotNull String getTitle(@NotNull Player player) {
        return title;
    }

    @NotNull
    public AbstractAdvancement setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public @NotNull String getDescription(@NotNull Player player) {
        return description;
    }

    @NotNull
    public AbstractAdvancement setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public @NotNull ItemStack getIcon(@NotNull Player player) {
        return icon;
    }

    @NotNull
    public AbstractAdvancement setIcon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public @NotNull AdvancementFrame getFrame(@NotNull Player player) {
        return frame;
    }

    @NotNull
    public AbstractAdvancement setFrame(AdvancementFrame frame) {
        this.frame = frame;
        return this;
    }

    @Override
    public boolean doToast(@NotNull Player player) {
        return showToast;
    }

    @NotNull
    public AbstractAdvancement setShowToast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    @Override
    public boolean doAnnouncement(@NotNull Player player) {
        return announce;
    }

    @NotNull
    public AbstractAdvancement setAnnounce(boolean announce) {
        this.announce = announce;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || ((obj instanceof Advancement) && ((Advancement) obj).getKey().equals(getKey()));
    }

}
