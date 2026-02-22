package com.stopwatch.sound;

import com.stopwatch.StopWatchConfig;
import net.runelite.client.audio.AudioPlayer;

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class SoundPlayer {

    @Inject
    private StopWatchConfig config;

    @Inject
    private AudioPlayer audioPlayer;

    public void playSound() {
        float gain = 20f * (float) Math.log10(config.alertVolume() / 100f);
        try {
            audioPlayer.play(SoundPlayer.class, "/sound/alert.wav", gain);
        } catch (Exception e) {
            log.warn("Failed to play StopWatch alert", e);
        }
    }
}