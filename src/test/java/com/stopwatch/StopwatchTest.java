package com.stopwatch;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class StopwatchTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(StopWatchPlugin.class);
		RuneLite.main(args);
	}
}