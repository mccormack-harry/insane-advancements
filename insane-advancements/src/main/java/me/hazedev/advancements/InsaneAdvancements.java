package me.hazedev.advancements;

import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.Advancement;
import me.hazedev.advancements.api.event.AdvancementGrantEvent;
import me.hazedev.advancements.api.event.AdvancementProgressEvent;
import me.hazedev.advancements.api.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InsaneAdvancements extends AdvancementManager implements Listener {

    private NMSHandler nmsHandler;
    private BukkitTask autosaveTask;
    private final List<Advancement> advancementTabs = new ArrayList<>();

    public InsaneAdvancements() {}

    @Override
    public void onEnable() {
        if (setupNMSHandler()) {
            Bukkit.getPluginManager().registerEvents(this, this);
            nmsHandler.addPacketListeners(Bukkit.getOnlinePlayers());
            reload();
            instance = this;
        }
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        saveAll();
        if (nmsHandler != null) {
            nmsHandler.removePacketListeners(Bukkit.getOnlinePlayers());
        }
        instance = null;
    }

    public void reload() {
        if (isEnabled()) {
            saveDefaultConfig();
            reloadConfig();
            long autosaveDelay = 20 * getConfig().getLong("autosave-delay", 300);
            autosaveTask = Bukkit.getScheduler().runTaskTimer(this, this::saveAll, autosaveDelay, autosaveDelay);
        }
    }

    @Override
    public void saveAll() {
        advancementTabs.forEach(tab -> tab.saveProgress(true));
    }

    private boolean setupNMSHandler() {
        if (nmsHandler != null) return true;

        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1);
        String nmsPackage = "me.hazedev.advancements.nms." + version + ".";
        try {
            Class<?> nmsHandlerClazz = Class.forName(nmsPackage + "NMSHandler");
            Constructor<?> constructor = nmsHandlerClazz.getConstructor(AdvancementManager.class);
            this.nmsHandler = (NMSHandler) constructor.newInstance(this);
            getLogger().info("NMSHandler loaded for version " + version);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            getLogger().severe("This version (" + version + ") of spigot is not supported yet!");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    @Override
    public @NotNull File getProgressDir() {
        return new File(getDataFolder(), "progress");
    }

    @Override
    public NMSHandler getNmsHandler() {
        return nmsHandler;
    }

    @Override
    public void registerAdvancementTabs(@NotNull List<Advancement> advancements) {
        List<Advancement> tabs = advancements.stream().filter(advancement -> advancement != null && advancement.getParent() == null).collect(Collectors.toList());
        this.advancementTabs.addAll(tabs);
        tabs.forEach(tab -> tab.loadProgress(Bukkit.getOnlinePlayers(), true));
        Bukkit.getOnlinePlayers().forEach(player -> nmsHandler.sendAdvancementTabs(player, tabs));
    }

    @Override
    public void registerAdvancementTab(@NotNull Advancement... advancements) {
        registerAdvancementTabs(Arrays.asList(advancements));
    }

    @Override
    public @NotNull List<Advancement> getTabs() {
        return Collections.unmodifiableList(this.advancementTabs);
    }

    @Override
    public @Nullable Advancement getAdvancementTab(@NotNull NamespacedKey key) {
        for (Advancement tab: advancementTabs) {
            if (tab.getKey().equals(key)) {
                return tab;
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.advancementTabs.forEach(tab -> tab.loadProgress(event.getPlayer(), true));
        nmsHandler.addPacketListener(event.getPlayer());
        final UUID uniqueId = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                nmsHandler.sendAdvancementTabs(player, getTabs());
            }
        }, 2);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        nmsHandler.removePacketListener(event.getPlayer());
        nmsHandler.clearCache(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancementGrant(AdvancementGrantEvent event) {
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();
        Bukkit.broadcastMessage(player.getName() + " has made the advancement " + advancement.getTitle(player));
        nmsHandler.sendToasts(player, advancement);
        nmsHandler.sendAdvancementTab(player, advancement);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancementProgress(AdvancementProgressEvent event) {
        nmsHandler.updateAdvancement(event.getPlayer(), event.getAdvancement());
    }

}
