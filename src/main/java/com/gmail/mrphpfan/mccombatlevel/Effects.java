package com.gmail.mrphpfan.mccombatlevel;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Effects {

    public static Effects create(ConfigurationSection configSection) {
        boolean lightning = configSection.getBoolean("lightning");

        ConfigurationSection soundSection = configSection.getConfigurationSection("sound");
        String soundType = soundSection.getString("type");
        Sound sound = Sound.valueOf(soundType.toUpperCase(Locale.ENGLISH));

        float pitch = (float) soundSection.getDouble("pitch");
        float volume = (float) soundSection.getDouble("volume");

        return new Effects(lightning, sound, pitch, volume);
    }

    private final boolean lightning;

    private final Sound sound;
    private final float pitch;
    private final float volume;

    public Effects(boolean lightning, Sound sound, float pitch, float volume) {
        this.lightning = lightning;
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public void playEffect(Player player) {
        Location location = player.getLocation();
        if (lightning) {
            player.getWorld().strikeLightningEffect(location);
        }

        if (sound != null) {
            player.playSound(location, sound, volume, pitch);
        }
    }
}
