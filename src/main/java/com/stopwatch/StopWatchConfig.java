package com.stopwatch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;

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
