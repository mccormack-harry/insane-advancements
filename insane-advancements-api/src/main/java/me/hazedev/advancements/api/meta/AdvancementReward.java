package me.hazedev.advancements.api.meta;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

/**
 * This functional interface allows you to easily define a custom function reward which can be added to an advancement. There are some default methods for creating common rewards.
 */
@FunctionalInterface
public interface AdvancementReward {

    void grant(@NotNull Player player);

    /**
     * @param itemStack An item to give
     * @return The reward
     */
    @NotNull
    static AdvancementReward item(@NotNull ItemStack itemStack) {
        return player -> player.getInventory().addItem(itemStack);
    }

    /**
     * @param experience An amount of experience to reward
     * @return The reward
     */
    @NotNull
    static AdvancementReward experience(int experience) {
        return player -> player.giveExp(experience);
    }

    /**
     * @param levels An amount of levels to reward
     * @return The reward
     */
    @NotNull
    static AdvancementReward levels(int levels) {
        return player -> player.giveExpLevels(levels);
    }

    /**
     * @param message A message to send to the player
     * @return The reward
     */
    @NotNull
    static AdvancementReward message(@NotNull String message) {
        return player -> player.sendMessage(message);
    }

    /**
     * @param message A message to announce globally in chat
     * @return The reward
     */
    @NotNull
    static AdvancementReward announce(@NotNull String message) {
        return player -> Bukkit.broadcastMessage(message);
    }

    /**
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeIn Fade in duration
     * @param stay Title duration
     * @param fadeOut Fade out duration
     * @return The reward
     */
    @NotNull
    static AdvancementReward title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return player -> player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * @param sound Sound to play
     * @param category SoundCategory
     * @param volume Volume
     * @param pitch Pitch
     * @return The reward
     */
    @NotNull
    static AdvancementReward sound(Sound sound, SoundCategory category, float volume, float pitch) {
        return player -> player.playSound(player.getLocation(), sound, category, volume, pitch);
    }

    /**
     * Plays a note to the player
     * @param instrument The instrument
     * @param note The note
     * @return The reward
     */
    @NotNull
    static AdvancementReward note(Instrument instrument, Note note) {
        return player -> player.playNote(player.getLocation(), instrument, note);
    }

    /**
     * Plays an effect to just the player
     * @param effect The effect
     * @param data The effect data
     * @param <T> The type of data
     * @return The reward
     */
    static <T> AdvancementReward effect(Effect effect, T data) {
        return player -> player.playEffect(player.getLocation(), effect, data);
    }

    /**
     * Plays an effect to the player, visible to nearby players
     * @param effect The effect
     * @return The reward
     */
    static AdvancementReward effect(EntityEffect effect) {
        return player -> player.playEffect(effect);
    }


    /**
     * Adds a potion effect to the player
     * @param effect The effect
     * @param force Whether to force the effect and remove conflicting
     * @return The reward
     */
    static AdvancementReward effect(PotionEffect effect, boolean force) {
        return player -> player.addPotionEffect(effect, force);
    }

}
