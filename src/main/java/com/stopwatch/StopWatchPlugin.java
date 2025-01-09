package com.stopwatch;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.NavigationButton;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Stop Watch",
	description = "A simple stopwatch with start, stop, and reset functionality.",
	tags = {"timer", "stop", "watch", "utility"}
)
public class StopWatchPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ClientThread clientThread;

	private StopWatchPanel panel;
	private NavigationButton navButton;

	@Inject
	private StopWatchConfig config;

	@Override
	protected void startUp()
	{
		log.info("StopWatch Plugin started!");

		// Schedule the initialization on the client thread
		clientThread.invokeLater(() ->
		{
			panel = new StopWatchPanel();

			navButton = NavigationButton.builder()
					.tooltip("Stopwatch")
					.icon(ImageUtil.loadImageResource(getClass(), "/stopwatch_icon.png")) // Add the BufferedImage for the sidebar
					.priority(5)
					.panel(panel)
					.build();

			clientToolbar.addNavigation(navButton);
		});

	}

	@Override
	protected void shutDown()
	{
		log.info("StopWatch Plugin stopped!");

		clientThread.invokeLater(() ->
		{
			clientToolbar.removeNavigation(navButton);
		});
	}

	@Provides
	StopWatchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StopWatchConfig.class);
	}

}
