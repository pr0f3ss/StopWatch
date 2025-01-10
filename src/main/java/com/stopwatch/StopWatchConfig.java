package com.stopwatch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface StopWatchConfig extends Config
{
	@ConfigItem(
		keyName = "useSound",
		name = "Enable Sound Notifications",
		description = "Toggles sound notification when the countdown reaches 0.",
		position = 1
	)
	default boolean useSound()
	{
		return true;
	}
}
