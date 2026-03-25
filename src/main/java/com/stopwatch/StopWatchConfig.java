package com.stopwatch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

@ConfigGroup("stopWatchPlugin")
public interface StopWatchConfig extends Config {
    @ConfigItem(
            keyName = "enableNotification",
            name = "Enable Notifications",
            description = "Toggle notification when the countdown reaches 0.",
            position = 1
    )
    default Notification enableNotification() {
        return Notification.ON;
    }    
    
        @ConfigItem(
            keyName = "useSound",
            name = "Enable Sound Notifications",
            description = "Toggles sound notification when the countdown reaches 0.",
            position = 2
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
            description = "Adjust the volume of the timer alert when sound notifications are enabled.",
            position = 3
    )
    default int alertVolume() {
        return 100;
    }

    @ConfigItem(
            keyName = "lastTimerMin",
            name = "Last timer minutes",
            description = "Persisted automatically",
            hidden = true
    )
    default int lastTimerMin() { return 0; }

    @ConfigItem(
            keyName = "lastTimerSec",
            name = "Last timer seconds",
            description = "Persisted automatically",
            hidden = true
    )
    default int lastTimerSec() { return 30; }

    @ConfigItem(
            keyName = "customPresets",
            name = "Custom presets",
            description = "Persisted automatically",
            hidden = true
    )
    default String customPresets() { return ""; }
}
