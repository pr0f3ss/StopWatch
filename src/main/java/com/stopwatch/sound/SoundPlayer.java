package com.stopwatch.sound;

import com.stopwatch.StopWatchConfig;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Slf4j
public class SoundPlayer {
    @Inject
    private StopWatchConfig config;

    private Clip clip = null;

    private boolean loadClip() {
        try (InputStream soundStream = getClass().getResourceAsStream("/sound/alert.wav")) {
            if (soundStream == null) {
                return false; // Sound file not found
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundStream);
            clip.open(audioInputStream);
            return true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            log.warn("Failed to load StopWatch alert", e);
        }
        return false;
    }

    // Code from the C-Engineer-Completed plugin
    // with slight modifications
    public void playSound() {
        if (clip == null || !clip.isOpen()) {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }

            try {
                clip = AudioSystem.getClip();
            } catch (LineUnavailableException e) {
                log.warn("Failed to retrieve clip for Stop Watch and Timer plugin", e);
                return;
            }

            if (!loadClip()) {
                return;
            }
        }

        // User configurable volume
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain = 20f * (float) Math.log10(config.alertVolume() / 100f);
        gain = Math.min(gain, volume.getMaximum());
        gain = Math.max(gain, volume.getMinimum());
        volume.setValue(gain);

        // From RuneLite base client Notifier class:
        // Using loop instead of start + setFramePosition prevents the clip
        // from not being played sometimes, presumably a race condition in the
        // underlying line driver
        // Resetting the clip position to the start to prevent the playback being stuck at the loop's end point
        clip.setFramePosition(0);
        clip.loop(0);
    }

    public void close() {
        if (clip != null && clip.isOpen()) {
            clip.close();
        }
    }
}