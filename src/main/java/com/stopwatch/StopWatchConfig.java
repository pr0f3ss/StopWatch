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
            keyName = "showMilliseconds",
            name = "Show milliseconds",
            description = "This gives you the option to hide the milliseconds when the timer is running.",
            position = 2
    )
    default boolean showMilliseconds() {
        return true;
    }

    @ConfigItem(
            keyName = "showSeconds",
            name = "Show seconds",
            description = "This gives you the option to hide the seconds when the timer is running.",
            position = 3
    )
    default boolean showSeconds() {
        return true;
    }

    @ConfigItem(
            keyName = "showTensOfSeconds",
            name = "Show tens of seconds",
            description = "This gives you the option to hide the tens of seconds when the timer is running.",
            position = 4
    )
    default boolean showTensOfSeconds() {
        return true;
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
