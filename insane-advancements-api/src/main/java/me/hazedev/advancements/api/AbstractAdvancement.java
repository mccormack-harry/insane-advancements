package me.hazedev.advancements.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.hazedev.advancements.api.meta.AdvancementDisplay;
import me.hazedev.advancements.api.meta.AdvancementType;
import me.hazedev.advancements.api.meta.AdvancementReward;
import me.hazedev.advancements.api.event.AdvancementGrantEvent;
import me.hazedev.advancements.api.event.AdvancementProgressEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractAdvancement implements Advancement {

    protected final AdvancementManager manager;

    protected final NamespacedKey key;
    protected final List<Advancement> children = new ArrayList<>();
    protected final List<AdvancementReward> rewards = new ArrayList<>();
    protected final Map<UUID, Long> grantedMap = new HashMap<>();
    protected final Map<UUID, Integer> progressMap = new HashMap<>();

    protected AdvancementDisplay display;

    public AbstractAdvancement(@NotNull AdvancementManager manager, @NotNull NamespacedKey key) {
        this(manager, key, null);
    }

    public AbstractAdvancement(@NotNull AdvancementManager manager, @NotNull NamespacedKey key, @Nullable AdvancementDisplay display) {
        this.manager = manager;
        this.key = key;
        this.display = display != null ? display : new AdvancementDisplay();
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
    public File getProgressFile() {
        final String fileName = getKey().getKey().replace('/', File.separatorChar).replace('.', File.separatorChar) + ".json";
        return new File(getManager().getProgressDir(), fileName);
    }

    @NotNull
    protected JsonObject readJsonProgress(@NotNull File jsonFile) {
        if (jsonFile.exists()) {
            try (JsonReader jsonReader = new JsonReader(new FileReader(jsonFile))) {
                return new JsonParser().parse(jsonReader).getAsJsonObject();
            } catch (IOException e) {
                getManager().getLogger().severe("Failed to read json object from " + jsonFile.getPath());
                e.printStackTrace();
                Path jsonFilePath = jsonFile.toPath();
                getManager().getLogger().info("Renaming corrupt file to prevent data loss");
                try {
                    Files.move(jsonFilePath, jsonFilePath.resolveSibling(jsonFile.getName() + "-corrupt" + System.currentTimeMillis()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    getManager().getLogger().info("Failed to rename corrupt file, corrupt data may be overwritten");
                }
            }
        }
        return new JsonObject();
    }

    @Override
    public void saveProgress(boolean saveChildren) {
        File progressFile = getProgressFile();
        JsonObject jsonProgressMap = readJsonProgress(progressFile);

        Set<UUID> keepLoaded = Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toSet());

        for (Iterator<Map.Entry<UUID, Long>> it = grantedMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Long> entry = it.next();
            UUID uniqueId = entry.getKey();
            JsonObject grantedTime = new JsonObject();
            grantedTime.addProperty("granted", entry.getValue());
            jsonProgressMap.add(uniqueId.toString(), grantedTime);
            if (!keepLoaded.contains(uniqueId)) {
                it.remove();
            }
        }
        for (Iterator<Map.Entry<UUID, Integer>> it = progressMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Integer> progressEntry = it.next();
            UUID uniqueId = progressEntry.getKey();
            JsonObject progress = new JsonObject();
            progress.addProperty("progress", progressEntry.getValue());
            jsonProgressMap.add(uniqueId.toString(), progress);
            if (!keepLoaded.contains(uniqueId)) {
                it.remove();
            }
        }

        if (jsonProgressMap.size() > 0) {
            if (!progressFile.exists()) //noinspection ResultOfMethodCallIgnored
                progressFile.getParentFile().mkdirs();
            try (FileWriter fileWriter = new FileWriter(progressFile)) {
                new Gson().toJson(jsonProgressMap, fileWriter);
            } catch (IOException e) {
                getManager().getLogger().severe("Failed to save progress for advancement " + getKey());
                e.printStackTrace();
            }
        }

        if (saveChildren) {
            this.children.forEach(child -> child.saveProgress(true));
        }
    }

    @Override
    public void loadProgress(@NotNull Collection<? extends Player> players, boolean loadChildren) {
        if (!players.isEmpty()) {
            File progressFile = getProgressFile();
            if (progressFile.exists()) {
                JsonObject jsonProgressMap = readJsonProgress(progressFile);
                for (UUID uniqueId : players.stream().filter(Objects::nonNull).map(Entity::getUniqueId).collect(Collectors.toList())) {
                    try {
                        JsonObject progress = jsonProgressMap.get(uniqueId.toString()).getAsJsonObject();
                        if (progress.has("granted")) {
                            grantedMap.put(uniqueId, progress.get("granted").getAsLong());
                        } else if (progress.has("progress")) {
                            progressMap.put(uniqueId, progress.get("progress").getAsInt());
                        }
                    } catch (Exception e) {
                        getManager().getLogger().warning("[" + getKey() + "] Failed to read progress for: " + uniqueId);
                    }
                }
            }
            if (loadChildren) {
                this.children.forEach(child -> child.loadProgress(players, true));
            }
        }
    }

    @Override
    public void loadProgress(@NotNull Player player, boolean loadChildren) {
        loadProgress(Collections.singleton(player), loadChildren);
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
        if (child.getParent() == this) {
            this.children.add(child);
        }
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
        if (!isGranted(player)) {
            grantedMap.put(player.getUniqueId(), System.currentTimeMillis());
            for (AdvancementReward reward : rewards) {
                reward.grant(player);
            }
            Bukkit.getPluginManager().callEvent(new AdvancementGrantEvent(player, this));
        }
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

    public void setDisplay(@NotNull AdvancementDisplay display) {
        this.display = display;
    }

    @Override
    public boolean isVisible(@NotNull Player player) {
        return display.getVisibility().isVisible(this, player);
    }

    @Override
    public float getX(@NotNull Player player) {
        return display.getX();
    }

    @Override
    public float getY(@NotNull Player player) {
        return display.getY();
    }

    @Override
    public @NotNull String getTitle(@NotNull Player player) {
        return display.getTitle();
    }

    @Override
    public @NotNull String getDescription(@NotNull Player player) {
        return display.getDescription();
    }

    @Override
    public @NotNull ItemStack getIcon(@NotNull Player player) {
        return display.getIcon();
    }

    @Override
    public @NotNull AdvancementType getType(@NotNull Player player) {
        return display.getType();
    }

    @Override
    public boolean isShowToast(@NotNull Player player) {
        return display.isShowToast();
    }

    @Override
    public @Nullable NamespacedKey getBackground(@NotNull Player player) {
        return display.getBackground();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || ((obj instanceof Advancement) && ((Advancement) obj).getKey().equals(getKey()));
    }

}
