# McCombatLevel

Bukkit plugin which displays a combat level above users heads and in chat based on their mcMMO stats that
affect combat.

## Commands

* /level or /combatlevel - Shows your current combat level

## Config

```Yaml
# True by default, this is the prefix to the left of the name in chat.
enable_prefix: true

#  True by default, this is the level that appears above the player's head
enable_tag_level: true

# "&aCombat" by default, this is the text that appears after the level above the player's head
tag_name: '&aCombat'

# GOLD by default, change the color of the brackets in the level prefix
prefix_bracket_color: GOLD

# DARK_GREEN by default, change the color of the level in the prefix
prefix_level_color: DARK_GREEN

# This message will be sent if the combat level change. You can use {0} for the old level and {1} for the new level
# If this is empty no message will be sent.
levelUpMessage: ''

# Math.round((unarmed + swords + axes + archery + .25 * acrobatics + .25 * taming) / 45) by default
# How the combat is calculated
formula: 'Math.round((unarmed + swords + axes + archery + .25 * acrobatics + .25 * taming) / 45)'

# level up effects
effect:
    lightning: false
    sound:
        # set this empty in order to disable it
        type: ''
        pitch: 0.1
        volume: 1

# Citizens npc integration
npc:
    # Should the integration be enabled; As soon as you activate the level feature the client will give them 0
    # as default anyway
    enabled: false
    # custom default value
    default: 0
    # Should this plugin look for offline players with the same name in the mcMMO database
    lookupOffline: true
```

## Images

![Chat format](https://i.imgur.com/J6M4ncp.png)
![Playertag format](https://i.imgur.com/tc1ikCH.png)

See:
* https://dev.bukkit.org/bukkit-plugins/mccombatlevel/
* https://www.curse.com/bukkit-plugins/minecraft/mccombatlevel

## Development builds

Development builds of this project can be acquired at the provided CI (continuous integration) server. It contains the
latest changes from the Source-Code in preparation for the following release. This means they could contain new
features, bug fixes and other changes since the last release.

Nevertheless builds are only tested using a small set of automated and a few manual tests. Therefore they **could**
contain new bugs and are likely to be less stable than released versions.

https://ci.codemc.org/job/Games647/job/mcCombatLevel/changes
