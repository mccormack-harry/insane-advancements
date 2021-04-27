package me.hazedev.advancements.nms.v1_16_R3;

import com.google.gson.JsonElement;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import me.hazedev.advancements.api.AdvancementManager;
import me.hazedev.advancements.api.ToastNotification;
import me.hazedev.advancements.api.Advancement;
import me.hazedev.advancements.api.meta.AdvancementType;
import me.hazedev.advancements.api.event.AdvancementScreenCloseEvent;
import me.hazedev.advancements.api.event.AdvancementTabChangeEvent;
import net.minecraft.server.v1_16_R3.AdvancementDisplay;
import net.minecraft.server.v1_16_R3.AdvancementFrameType;
import net.minecraft.server.v1_16_R3.AdvancementProgress;
import net.minecraft.server.v1_16_R3.AdvancementRewards;
import net.minecraft.server.v1_16_R3.Criterion;
import net.minecraft.server.v1_16_R3.CriterionTriggerImpossible;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.PacketPlayInAdvancements;
import net.minecraft.server.v1_16_R3.PacketPlayOutAdvancements;
import net.minecraft.server.v1_16_R3.PacketPlayOutSelectAdvancementTab;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class NMSHandler extends me.hazedev.advancements.api.nms.NMSHandler {

    Map<UUID, Map<NamespacedKey, net.minecraft.server.v1_16_R3.Advancement>> advancementCache = new HashMap<>();

    public NMSHandler(AdvancementManager manager) {
        super(manager);
    }

    @Override
    public void clearCache(@NotNull UUID uniqueId) {
        advancementCache.remove(uniqueId);
    }

    private void addToCache(@NotNull Player player, @NotNull net.minecraft.server.v1_16_R3.Advancement advancement) {
        advancementCache.putIfAbsent(player.getUniqueId(), new HashMap<>());
        Map<NamespacedKey, net.minecraft.server.v1_16_R3.Advancement> cache = advancementCache.get(player.getUniqueId());
        cache.put(convert(advancement.getName()), advancement);
    }

    @Nullable
    private net.minecraft.server.v1_16_R3.Advancement getCachedAdvancement(@NotNull Player player, @NotNull NamespacedKey key) {
        Map<NamespacedKey, net.minecraft.server.v1_16_R3.Advancement> cache = advancementCache.get(player.getUniqueId());
        if (cache != null)
            return cache.get(key);
        return null;
    }

    @Override
    public void removePacketListener(@NotNull Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getUniqueId().toString());
            return null;
        });
    }

    @Override
    public void addPacketListener(@NotNull Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                if (packet instanceof PacketPlayInAdvancements) {
                    PacketPlayInAdvancements packetPlayInAdvancements = (PacketPlayInAdvancements) packet;
                    PacketPlayInAdvancements.Status action = packetPlayInAdvancements.c();
                    if (action == PacketPlayInAdvancements.Status.OPENED_TAB) {
                        NamespacedKey key = convert(packetPlayInAdvancements.d());
                        Advancement tab = manager.getAdvancementTab(key);
                        Bukkit.getScheduler().runTask(manager, () -> Bukkit.getPluginManager().callEvent(new AdvancementTabChangeEvent(player, key, tab)));
                    } else if (action == PacketPlayInAdvancements.Status.CLOSED_SCREEN) {
                        Bukkit.getScheduler().runTask(manager, () -> Bukkit.getPluginManager().callEvent(new AdvancementScreenCloseEvent(manager, player)));
                    }
                }
                super.channelRead(ctx, packet);
            }
        };
        ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", player.getUniqueId().toString(), channelDuplexHandler);
    }

    @Override
    public void sendAdvancementTabs(@NotNull Player player, @NotNull List<Advancement> advancements) {
        Set<Advancement> roots = advancements.stream().filter(Objects::nonNull).map(Advancement::getRoot).collect(Collectors.toSet());
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        for (Advancement root: roots) {
            packetBuilder.addAdvancementAndChildren(root, null);
        }
        packetBuilder.send();
    }

    @Override
    public void sendAdvancementTab(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        packetBuilder.addAdvancementAndChildren(advancement.getRoot(), null);
        packetBuilder.send();
    }

    public void updateAdvancement(@NotNull Player player, @NotNull Advancement advancement) {

        Advancement parent = advancement.getParent();
        net.minecraft.server.v1_16_R3.Advancement nmsParent = null;
        if (parent != null) {
            nmsParent = getCachedAdvancement(player, parent.getKey());
            if (nmsParent == null) {
                return;
            }
        }

        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        packetBuilder.addAdvancement(advancement, nmsParent);

        packetBuilder.send();
    }

    @Override
    public void sendToasts(@NotNull Player player, @NotNull Advancement... advancements) {
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        packetBuilder.setShowToasts(true);
        packetBuilder.setDoCache(false);
        for (Advancement advancement: advancements) {
            packetBuilder.addAdvancement(advancement instanceof ToastNotification ? advancement : new ToastNotification(manager, advancement.getType(player), advancement.getIcon(player), advancement.getTitle(player)), null);
        }
        packetBuilder.send();

        AdvancementsPacketBuilder removePacketBuilder = new AdvancementsPacketBuilder(player);
        removePacketBuilder.remove.addAll(packetBuilder.nmsAdvancements.stream().map(net.minecraft.server.v1_16_R3.Advancement::getName).collect(Collectors.toList()));
        removePacketBuilder.send();
    }

    @Override
    public void setActiveTab(@NotNull Player player, @NotNull NamespacedKey key) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSelectAdvancementTab(convert(key)));
    }

    @Override
    public void clearAdvancements(@NotNull Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutAdvancements(true, new ArrayList<>(), new HashSet<>(), new HashMap<>()));
    }

    public static MinecraftKey convert(@NotNull NamespacedKey key) {
        return new MinecraftKey(key.getNamespace(), key.getKey());
    }

    public static NamespacedKey convert(@NotNull MinecraftKey key) {
        return new NamespacedKey(key.getNamespace(), key.getKey());
    }

    public static net.minecraft.server.v1_16_R3.ItemStack convert(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }

    public static AdvancementFrameType convert(@NotNull AdvancementType type) {
        switch (type) {
            case CHALLENGE:
                return AdvancementFrameType.CHALLENGE;
            case GOAL:
                return AdvancementFrameType.GOAL;
            case TASK:
            default:
                return AdvancementFrameType.TASK;
        }
    }

    public static IChatBaseComponent convert(@NotNull JsonElement json) {
        return IChatBaseComponent.ChatSerializer.a(json);
    }

    public class AdvancementsPacketBuilder {

        private final Player player;
        private boolean doCache = true;
        private boolean showToasts = false;

        protected final Collection<net.minecraft.server.v1_16_R3.Advancement> nmsAdvancements = new ArrayList<>();
        protected final Set<MinecraftKey> remove = new HashSet<>();
        protected final Map<MinecraftKey, AdvancementProgress> progressMap = new HashMap<>();

        public AdvancementsPacketBuilder(@NotNull Player player) {
            this.player = player;
        }

        public void setDoCache(boolean doCache) {
            this.doCache = doCache;
        }

        public void setShowToasts(boolean showToasts) {
            this.showToasts = showToasts;
        }

        public void addAdvancementAndChildren(@NotNull Advancement advancement, @Nullable net.minecraft.server.v1_16_R3.Advancement parent) {
            if (advancement.isVisible(player)) {
                net.minecraft.server.v1_16_R3.Advancement nmsAdvancement = addAdvancement(advancement, parent);
                for (Advancement child : advancement.getChildren()) {
                    addAdvancementAndChildren(child, nmsAdvancement);
                }
            }
        }

        @NotNull
        public net.minecraft.server.v1_16_R3.Advancement addAdvancement(@NotNull Advancement advancement, @Nullable net.minecraft.server.v1_16_R3.Advancement parent) {

            net.minecraft.server.v1_16_R3.AdvancementProgress nmsProgress = new net.minecraft.server.v1_16_R3.AdvancementProgress();
            Map<String, Criterion> criteria = new HashMap<>();
            Criterion criterion = new Criterion(new CriterionTriggerImpossible.a());
            int goal = advancement.getGoal(player);
            int progress = advancement.getProgress(player);
            for (int i = 0; i < goal; i++) {
                criteria.put("criterion." + i, criterion);
            }
            String[][] requirements = criteria.keySet().stream().map(s -> new String[]{s}).toArray(String[][]::new);
            nmsProgress.a(criteria, requirements);
            int awarded = 0;
            for (String criterionName : nmsProgress.getRemainingCriteria()) {
                if (awarded < progress) {
                    nmsProgress.getCriterionProgress(criterionName).b(); // Award criteria
                    ++awarded;
                }
            }

            MinecraftKey nmsBackgroundKey = null;
            if (advancement.getParent() == null) {
                NamespacedKey backgroundKey = advancement.getBackground(player);
                if (backgroundKey != null)
                    nmsBackgroundKey = convert(backgroundKey);
            }
            boolean isHidden = !advancement.isVisible(player);
            AdvancementDisplay nmsDisplay = new AdvancementDisplay(convert(advancement.getIcon(player)), convert(advancement.getJsonTitle(player)), convert(advancement.getJsonDescription(player)), nmsBackgroundKey, convert(advancement.getType(player)), showToasts && advancement.isShowToast(player), false, isHidden);
            nmsDisplay.a(advancement.getX(player), advancement.getY(player));
            AdvancementRewards nmsRewards = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], null);
            net.minecraft.server.v1_16_R3.Advancement nmsAdvancement = new net.minecraft.server.v1_16_R3.Advancement(convert(advancement.getKey()), parent, nmsDisplay, nmsRewards, criteria, requirements);

            if (doCache) {
                addToCache(player, nmsAdvancement);
            }
            progressMap.put(nmsAdvancement.getName(), nmsProgress);
            nmsAdvancements.add(nmsAdvancement);
            return nmsAdvancement;
        }

        public void send() {
            PacketPlayOutAdvancements packet = new PacketPlayOutAdvancements(false, nmsAdvancements, remove, progressMap);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }

    }

}
