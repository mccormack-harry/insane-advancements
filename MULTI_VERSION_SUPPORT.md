# Multi-Version Support Implementation

This document explains the multi-version support implementation for the Insane Advancements plugin, providing NMS (Net Minecraft Server) compatibility across Minecraft versions 1.12 through 1.21.

## Architecture Overview

The plugin uses a modular architecture where each Minecraft version has its own NMS handler module:

```
insane-advancements/
├── insane-advancements-api/           # Core API (version-independent)
├── insane-advancements/               # Main plugin module
├── insane-advancements-v1_12_R1/      # 1.12.x support
├── insane-advancements-v1_13_R1/      # 1.13.x support
├── insane-advancements-v1_13_R2/      # 1.13.2 support
├── insane-advancements-v1_14_R1/      # 1.14.x support
├── insane-advancements-v1_15_R1/      # 1.15.x support
├── insane-advancements-v1_16_R1/      # 1.16.1-1.16.3 support
├── insane-advancements-v1_16_R2/      # 1.16.4 support
├── insane-advancements-v1_16_R3/      # 1.16.5 support (original)
├── insane-advancements-v1_17_R1/      # 1.17.x support
├── insane-advancements-v1_18_R1/      # 1.18.x support
├── insane-advancements-v1_18_R2/      # 1.18.2 support
├── insane-advancements-v1_19_R1/      # 1.19.x support
├── insane-advancements-v1_19_R2/      # 1.19.2 support
├── insane-advancements-v1_19_R3/      # 1.19.3-1.19.4 support
├── insane-advancements-v1_20_R1/      # 1.20.x support
├── insane-advancements-v1_20_R2/      # 1.20.2 support
├── insane-advancements-v1_20_R3/      # 1.20.3-1.20.4 support
├── insane-advancements-v1_20_R4/      # 1.20.5-1.20.6 support
└── insane-advancements-v1_21_R1/      # 1.21.x support
```

## Version Detection and Loading

The main plugin (`InsaneAdvancements.java`) uses reflection to dynamically load the appropriate NMS handler:

```java
private boolean setupNMSHandler() {
    String name = Bukkit.getServer().getClass().getPackage().getName();
    String version = name.substring(name.lastIndexOf('.') + 1);
    String nmsPackage = "me.hazedev.advancements.nms." + version + ".";
    // Dynamically load the appropriate NMSHandler class
}
```

## API Changes Across Versions

### Legacy Versions (1.12 - 1.16)

These versions use the traditional NMS structure:
- Package: `net.minecraft.server.vX_X_RX.*`
- Classes: `IChatBaseComponent`, `MinecraftKey`, `PacketPlayInAdvancements`, etc.
- CraftBukkit: `org.bukkit.craftbukkit.vX_X_RX.*`

### Modern Versions (1.17+)

Starting with 1.17, Minecraft moved to Mojang mappings:
- Package: `net.minecraft.*` (organized by functionality)
- Classes: `Component`, `ResourceLocation`, `ServerboundSeenAdvancementsPacket`, etc.
- Connection API: `connection.send()` instead of `playerConnection.sendPacket()`

## Key Implementation Details

### NMS Handler Interface

All version-specific handlers implement the abstract `NMSHandler` class:

```java
public abstract class NMSHandler {
    public abstract void clearCache(@NotNull UUID uniqueId);
    public abstract void addPacketListener(@NotNull Player player);
    public abstract void removePacketListener(@NotNull Player player);
    public abstract void sendAdvancementTabs(@NotNull Player player, @NotNull List<Advancement> advancements);
    public abstract void sendAdvancementTab(@NotNull Player player, @NotNull Advancement advancement);
    public abstract void updateAdvancement(Player player, Advancement advancement);
    public abstract void sendToasts(@NotNull Player player, @NotNull Advancement... advancements);
    public abstract void setActiveTab(@NotNull Player player, @NotNull NamespacedKey key);
    public abstract void clearAdvancements(@NotNull Player player);
}
```

### Packet Handling

Each version handles advancement-related packets differently:

**Legacy (1.12-1.16):**
```java
if (packet instanceof PacketPlayInAdvancements) {
    PacketPlayInAdvancements.Status action = packet.c();
    // Handle OPENED_TAB, CLOSED_SCREEN actions
}
```

**Modern (1.17+):**
```java
if (packet instanceof ServerboundSeenAdvancementsPacket) {
    ServerboundSeenAdvancementsPacket.Action action = packet.getAction();
    // Handle OPENED_TAB, CLOSED_SCREEN actions
}
```

### Component Serialization

**Legacy:**
```java
IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
```

**Modern:**
```java
Component component = Component.Serializer.fromJson(json);
```

## Building and Dependencies

Each module has its own Maven POM with the appropriate Spigot dependency:

```xml
<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot</artifactId>
        <version>${version}-R0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>me.hazedev.advancements</groupId>
        <artifactId>insane-advancements-api</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

## Future Maintenance

When new Minecraft versions are released:

1. Create a new module: `insane-advancements-vX_XX_RX`
2. Copy the appropriate template (legacy or modern based on version)
3. Update package imports and class names as needed
4. Add the module to the parent POM
5. Test with the new server version

## Limitations and Notes

- **Compilation**: Individual modules can only be compiled against their specific server versions
- **Testing**: Runtime testing requires actual server environments for each version
- **API Evolution**: Future versions may require additional adaptations beyond the current template approach
- **Performance**: The reflection-based loading has minimal impact as it only occurs once during plugin startup

## Supported Version Matrix

| Minecraft Version | NMS Version | Module Name | Status |
|-------------------|-------------|-------------|---------|
| 1.12.x | v1_12_R1 | insane-advancements-v1_12_R1 | ✅ Implemented |
| 1.13.x | v1_13_R1 | insane-advancements-v1_13_R1 | ✅ Implemented |
| 1.13.2 | v1_13_R2 | insane-advancements-v1_13_R2 | ✅ Implemented |
| 1.14.x | v1_14_R1 | insane-advancements-v1_14_R1 | ✅ Implemented |
| 1.15.x | v1_15_R1 | insane-advancements-v1_15_R1 | ✅ Implemented |
| 1.16.1-1.16.3 | v1_16_R1 | insane-advancements-v1_16_R1 | ✅ Implemented |
| 1.16.4 | v1_16_R2 | insane-advancements-v1_16_R2 | ✅ Implemented |
| 1.16.5 | v1_16_R3 | insane-advancements-v1_16_R3 | ✅ Implemented |
| 1.17.x | v1_17_R1 | insane-advancements-v1_17_R1 | ✅ Implemented |
| 1.18.x | v1_18_R1 | insane-advancements-v1_18_R1 | ✅ Implemented |
| 1.18.2 | v1_18_R2 | insane-advancements-v1_18_R2 | ✅ Implemented |
| 1.19.x | v1_19_R1 | insane-advancements-v1_19_R1 | ✅ Implemented |
| 1.19.2 | v1_19_R2 | insane-advancements-v1_19_R2 | ✅ Implemented |
| 1.19.3-1.19.4 | v1_19_R3 | insane-advancements-v1_19_R3 | ✅ Implemented |
| 1.20.x | v1_20_R1 | insane-advancements-v1_20_R1 | ✅ Implemented |
| 1.20.2 | v1_20_R2 | insane-advancements-v1_20_R2 | ✅ Implemented |
| 1.20.3-1.20.4 | v1_20_R3 | insane-advancements-v1_20_R3 | ✅ Implemented |
| 1.20.5-1.20.6 | v1_20_R4 | insane-advancements-v1_20_R4 | ✅ Implemented |
| 1.21.x | v1_21_R1 | insane-advancements-v1_21_R1 | ✅ Implemented |

This implementation provides comprehensive support for all major Minecraft versions from 1.12 (when advancements were first introduced) through the latest 1.21 release.