package com.stopwatch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("stopWatchPlugin")
public interface StopWatchConfig extends Config {
    @ConfigItem(
            keyName = "useSound",
            name = "Enable Sound Notifications",
            description = "Toggles sound notification when the countdown reaches 0.",
            position = 1
    )
    default boolean useSound() {
        return true;
    }

    @Range(
            min = 0,
            max = 200
    )
    @ConfigItem(
            keyName = "alertVolume",
            name = "Timer Alert Volume",
            description = "Adjust the volume of the timer alert.",
            position = 2
    )
    default int alertVolume() {
        return 100;
    }
}
