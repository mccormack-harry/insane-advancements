# Insane Advancements
Insane Advancements is my rewrite of [ZockerAxel's CrazyAdvancementsAPI](https://github.com/ZockerAxel/CrazyAdvancementsAPI)
- **Warning** - The API is in very early stages, could change significantly. Not reccomended for use in production servers yet.

## Download
[**Plugin**](https://github.com/haz8989/insane-advancements/releases) <br>
[**API Repository**](https://github.com/haz8989/insane-advancements/packages/)

## Features
- Easily create new advancements and tabs
- Highly customisable
- Dynamic Advancements per player
- Full control
- Automatic saving of progress using JSON
- Support for 1.16.5
- Easily send advancement toast notifications
- [Custom events](insane-advancements-api/src/main/java/me/hazedev/advancements/api/event)
- Set x,z co-ordinates of advancements in the menu

**Coming Very Soon**
- Support for all versions 1.12+
- Everything will be configurable in a config.yml file
- Automatically position advancements in menu (Multiple modes)
- Documentation

## Example Usage
You must download and install the server plugin from above, and add the API as a dependency in your maven/gradle configuration.
```java
public class InsaneExample extends JavaPlugin implements Listener {

    private final AdvancementManager manager;
    private final Advancement root;
    private final Advancement sneak;
    private final Advancement mine;

    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().isPluginEnabled("InsaneAvancements")) {
            manager = (AdvancementManager) Bukkit.getPluginManager.getPlugin("InsaneAdvancements");
        } else {
            getLogger().severe("InsaneAdvancements is required to be installed!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);

        // The root advancement is also the advancement tab
        root = new RootAdvancement(manager, "tutorial", "root")
                .setBackground("textures/block/stone.png")
                .setTitle("Tutorial")
                .setDescription("Welcome to the server!")
                .setIcon(new ItemStack(Material.GRASS_BLOCK))
                .setVisibility(AdvancementVisibility.ALWAYS)
                .setShowToast(false)
                .addRewards(AdvancementReward.message("&6Welcome to the server, press L to open the advancements tab!"));

        sneak = new InsaneAdvancement(root, "sneak", 1)
                .setTitle("Ninja")
                .setDescription("Press Shift to Sneak!")
                .setFrame(AdvancementFrame.TASK) // Default frame
                .setIcon(new ItemStack(Material.LEATHER_BOOTS))
                .setVisibility(AdvancementVisibility.ALWAYS)

        mine = new InsaneAdvancement(sneak, "mine", 10)
                .setTitle("New Miner")
                .setDescription("Break 10 blocks")
                .setFrame(AdvancementFrame.GOAL)
                .setIcon(new ItemStack(Material.WOODEN_PICAXE))
                .setVisibility(AdvancementVisibility.PARENT_GRANTED)
                .addRewards(AdvancementReward.experience(50));

        manager.registerAdvancementTab(root);

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            root.grant(event.getPlayer());
        }, 5);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        sneak.grant(event.getPlayer);
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        mine.addProgress(event.getPlayer(), 1)
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent) {
        // Example sending an advancement toast without a real advancement
        Advancement toast = new ToastNotification(manager, AdvancementFrame.CHALLENGE, new ItemStack(Material.BARRIER), "&cDon't do that!");
        manager.getNmsHandler().sendToasts(toast);
        event.setCancelled(true);
    }

}
```