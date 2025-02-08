package com.stopwatch;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.stopwatch.sound.SoundPlayer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.NavigationButton;
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
    private StopWatchConfig config;

    @Inject
    private SoundPlayer soundPlayer;

    private NavigationButton navButton;

    @Override
    protected void startUp() {
        log.info("StopWatch Plugin started!");
        StopWatchPanel panel = new StopWatchPanel(config, soundPlayer);

        navButton = NavigationButton.builder()
                .tooltip("Stopwatch and Timer")
                .icon(ImageUtil.loadImageResource(getClass(), "/images/stopwatch_icon.png"))
                .priority(5)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        clientToolbar.removeNavigation(navButton);
        soundPlayer.close();
        log.info("StopWatch Plugin stopped!");
    }

    @Provides
    StopWatchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(StopWatchConfig.class);
    }

}
