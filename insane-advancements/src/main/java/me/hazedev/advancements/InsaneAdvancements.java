package me.hazedev.advancements;

import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.advancement.Advancement;
import me.hazedev.advancements.api.event.AdvancementGrantEvent;
import me.hazedev.advancements.api.event.AdvancementProgressEvent;
import me.hazedev.advancements.api.nms.NMSHandler;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    private final List<Advancement> advancementTabs = new ArrayList<>();

    @Override
    public void onEnable() {
        if (setupNMSHandler()) {
            Bukkit.getPluginManager().registerEvents(this, this);
            nmsHandler.addPacketListeners(Bukkit.getOnlinePlayers());
        }
    }

    @Override
    public void onDisable() {
        saveAll();
        if (nmsHandler != null) {
            nmsHandler.removePacketListeners(Bukkit.getOnlinePlayers());
        }
    }

    @Override
    public void saveAll() {
        advancementTabs.forEach(tab -> Bukkit.getOnlinePlayers().forEach(tab::saveProgress));
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
        Bukkit.getOnlinePlayers().forEach(player -> {
            final UUID uniqueId = player.getUniqueId();
            tabs.forEach(tab -> tab.loadProgress(uniqueId));
            nmsHandler.sendAdvancementTabs(player, tabs);
        });
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
        Bukkit.getScheduler().runTaskLater(this, () -> {
            nmsHandler.addPacketListener(event.getPlayer());
            advancementTabs.forEach(tab -> tab.loadProgress(event.getPlayer()));
            nmsHandler.sendAdvancementTabs(event.getPlayer(), advancementTabs);
        }, 5);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        nmsHandler.removePacketListener(event.getPlayer());
        advancementTabs.forEach(advancement -> advancement.saveProgress(event.getPlayer()));
        nmsHandler.clearCache(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancementGrant(AdvancementGrantEvent event) {
        Bukkit.broadcastMessage(event.getPlayer().getName() + " granted " + event.getAdvancement().getKey());
        nmsHandler.sendToasts(event.getPlayer(), event.getAdvancement());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAdvancementProgress(AdvancementProgressEvent event) {
        nmsHandler.updateProgress(event.getPlayer(), event.getAdvancement());
    }

}
