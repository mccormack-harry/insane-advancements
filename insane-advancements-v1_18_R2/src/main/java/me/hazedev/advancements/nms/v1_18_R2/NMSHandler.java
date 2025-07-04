package me.hazedev.advancements.nms.v1_18_R2;

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
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementFrameType;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
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

    Map<UUID, Map<NamespacedKey, net.minecraft.advancements.Advancement>> advancementCache = new HashMap<>();

    public NMSHandler(AdvancementManager manager) {
        super(manager);
    }

    @Override
    public void clearCache(@NotNull UUID uniqueId) {
        advancementCache.remove(uniqueId);
    }

    private void addToCache(@NotNull Player player, @NotNull net.minecraft.advancements.Advancement advancement) {
        advancementCache.putIfAbsent(player.getUniqueId(), new HashMap<>());
        Map<NamespacedKey, net.minecraft.advancements.Advancement> cache = advancementCache.get(player.getUniqueId());
        cache.put(convert(advancement.getId()), advancement);
    }

    @Nullable
    private net.minecraft.advancements.Advancement getCachedAdvancement(@NotNull Player player, @NotNull NamespacedKey key) {
        Map<NamespacedKey, net.minecraft.advancements.Advancement> cache = advancementCache.get(player.getUniqueId());
        if (cache != null)
            return cache.get(key);
        return null;
    }

    @Override
    public void removePacketListener(@NotNull Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.getConnection().channel;
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
                if (packet instanceof ServerboundSeenAdvancementsPacket) {
                    ServerboundSeenAdvancementsPacket packetPlayInAdvancements = (ServerboundSeenAdvancementsPacket) packet;
                    ServerboundSeenAdvancementsPacket.Action action = packetPlayInAdvancements.getAction();
                    if (action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
                        NamespacedKey key = convert(packetPlayInAdvancements.getTab());
                        Advancement tab = manager.getAdvancementTab(key);
                        Bukkit.getScheduler().runTask(manager, () -> Bukkit.getPluginManager().callEvent(new AdvancementTabChangeEvent(player, key, tab)));
                    } else if (action == ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN) {
                        Bukkit.getScheduler().runTask(manager, () -> Bukkit.getPluginManager().callEvent(new AdvancementScreenCloseEvent(manager, player)));
                    }
                }
                super.channelRead(ctx, packet);
            }
        };
        ((CraftPlayer) player).getHandle().connection.getConnection().channel.pipeline().addBefore("packet_handler", player.getUniqueId().toString(), channelDuplexHandler);
    }

    @Override
    public void sendAdvancementTabs(@NotNull Player player, @NotNull List<Advancement> advancements) {
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        for (Advancement advancement : advancements) {
            packetBuilder.addAdvancementAndChildren(advancement, null);
        }
        packetBuilder.send();
    }

    @Override
    public void sendAdvancementTab(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        packetBuilder.addAdvancementAndChildren(advancement, null);
        packetBuilder.send();
    }

    @Override
    public void updateAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
        AdvancementsPacketBuilder packetBuilder = new AdvancementsPacketBuilder(player);
        packetBuilder.setDoCache(false);
        packetBuilder.addAdvancement(advancement, null);
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
        removePacketBuilder.remove.addAll(packetBuilder.nmsAdvancements.stream().map(net.minecraft.advancements.Advancement::getId).collect(Collectors.toList()));
        removePacketBuilder.send();
    }

    @Override
    public void setActiveTab(@NotNull Player player, @NotNull NamespacedKey key) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundSelectAdvancementsTabPacket(convert(key)));
    }

    @Override
    public void clearAdvancements(@NotNull Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundUpdateAdvancementsPacket(true, new ArrayList<>(), new HashSet<>(), new HashMap<>()));
    }

    public static ResourceLocation convert(@NotNull NamespacedKey key) {
        return new ResourceLocation(key.getNamespace(), key.getKey());
    }

    public static NamespacedKey convert(@NotNull ResourceLocation key) {
        return new NamespacedKey(key.getNamespace(), key.getPath());
    }

    public static net.minecraft.world.item.ItemStack convert(@NotNull ItemStack item) {
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

    public static Component convert(@NotNull JsonElement json) {
        return Component.Serializer.fromJson(json);
    }

    public class AdvancementsPacketBuilder {

        private final Player player;
        private boolean doCache = true;
        private boolean showToasts = false;

        protected final Collection<net.minecraft.advancements.Advancement> nmsAdvancements = new ArrayList<>();
        protected final Set<ResourceLocation> remove = new HashSet<>();
        protected final Map<ResourceLocation, AdvancementProgress> progressMap = new HashMap<>();

        public AdvancementsPacketBuilder(@NotNull Player player) {
            this.player = player;
        }

        public void setDoCache(boolean doCache) {
            this.doCache = doCache;
        }

        public void setShowToasts(boolean showToasts) {
            this.showToasts = showToasts;
        }

        public void addAdvancementAndChildren(@NotNull Advancement advancement, @Nullable net.minecraft.advancements.Advancement parent) {
            net.minecraft.advancements.Advancement nmsAdvancement = addAdvancement(advancement, parent);
            for (Advancement child : advancement.getChildren()) {
                addAdvancementAndChildren(child, nmsAdvancement);
            }
        }

        @NotNull
        public net.minecraft.advancements.Advancement addAdvancement(@NotNull Advancement advancement, @Nullable net.minecraft.advancements.Advancement parent) {
            ResourceLocation key = convert(advancement.getKey());
            AdvancementDisplay display = new AdvancementDisplay(convert(advancement.getIcon(player)), convert(advancement.getTitle(player)), convert(advancement.getDescription(player)), 
                    advancement.getBackgroundTexture() != null ? convert(advancement.getBackgroundTexture()) : null, convert(advancement.getType(player)), showToasts, advancement.isAnnounceToChat(), advancement.isHidden());
            if (advancement.getX() != null) display.setLocation(advancement.getX(), advancement.getY());

            Map<String, Criterion> criteria = new HashMap<>();
            String name = "impossible";
            criteria.put(name, new Criterion(new ImpossibleTrigger.TriggerInstance()));

            String[][] requirements = new String[1][1];
            requirements[0][0] = name;

            net.minecraft.advancements.Advancement nmsAdvancement = new net.minecraft.advancements.Advancement(key, parent, display, AdvancementRewards.EMPTY, criteria, requirements);
            nmsAdvancements.add(nmsAdvancement);

            AdvancementProgress advancementProgress = new AdvancementProgress();
            advancementProgress.update(criteria, requirements);
            if (advancement.isGranted(player)) {
                advancementProgress.getCriterion(name).grant();
            }
            progressMap.put(key, advancementProgress);

            if (doCache) {
                addToCache(player, nmsAdvancement);
            }
            return nmsAdvancement;
        }

        public void send() {
            ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, nmsAdvancements, remove, progressMap);
            ((CraftPlayer) player).getHandle().connection.send(packet);
        }

    }

}
