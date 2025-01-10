package com.stopwatch;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
        name = "Stopwatch and Timer",
        description = "Enable the Stopwatch and Timer panel, which contains simple stopwatch and countdown timer features for PVM.",
        tags = {"timer", "stop", "watch", "utility"}
)
public class StopWatchPlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ClientThread clientThread;

    private StopWatchPanel panel;
    private NavigationButton navButton;

    @Inject
    private StopWatchConfig config;

    @Override
    protected void startUp() {
        log.info("StopWatch Plugin started!");

        clientThread.invokeLater(() ->
        {
            panel = new StopWatchPanel(config);

            navButton = NavigationButton.builder()
                    .tooltip("Stopwatch and Timer")
                    .icon(ImageUtil.loadImageResource(getClass(), "/stopwatch_icon.png")) // Add the BufferedImage for the sidebar
                    .priority(5)
                    .panel(panel)
                    .build();

            clientToolbar.addNavigation(navButton);
        });
    }

    @Override
    protected void shutDown() {
        log.info("StopWatch Plugin stopped!");

        clientThread.invokeLater(() ->
        {
            clientToolbar.removeNavigation(navButton);
        });
    }

    @Provides
    StopWatchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(StopWatchConfig.class);
    }

}
