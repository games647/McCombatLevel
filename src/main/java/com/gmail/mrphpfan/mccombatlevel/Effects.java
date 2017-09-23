package com.gmail.mrphpfan.mccombatlevel;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Effects {

    public static Effects create(ConfigurationSection configSection) {
        boolean lightning = configSection.getBoolean("lightning");

        String effectType = configSection.getString("effect");
        Effect particleEffect = null;
        if (!effectType.isEmpty()) {
            Optional<Effect> optionalEffect = Enums.getIfPresent(Effect.class, effectType.toUpperCase());
            if (optionalEffect.isPresent()) {
                particleEffect = optionalEffect.get();
            }
        }

        ConfigurationSection soundSection = configSection.getConfigurationSection("sound");
        String soundType = soundSection.getString("type");

        Sound sound = null;
        Optional<Sound> optionalSound = Enums.getIfPresent(Sound.class, soundType.toUpperCase());
        if (optionalSound.isPresent()) {
            sound = optionalSound.get();
        }

        float pitch = (float) soundSection.getDouble("pitch");
        float volume = (float) soundSection.getDouble("volume");

        return new Effects(lightning, particleEffect, sound, pitch, volume);
    }

    private final boolean lightning;

    private final Effect particleEffect;

    private final Sound sound;
    private final float pitch;
    private final float volume;

    public Effects(boolean lightning, Effect particleEffect, Sound sound, float pitch, float volume) {
        this.lightning = lightning;
        this.particleEffect = particleEffect;

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

        if (particleEffect != null) {
            player.playEffect(location, particleEffect, particleEffect.getData());
        }
    }
}
