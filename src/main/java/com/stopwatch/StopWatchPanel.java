package com.stopwatch;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopWatchPanel extends PluginPanel
{
    private final JLabel timeLabel;
    private long startTime = 0;
    private long lastUpdateTime = 0;
    private boolean running = false;

    // Swing Timer to update the UI periodically
    private final Timer timer;

    public StopWatchPanel()
    {
        // Set layout for the main panel
        setLayout(new BorderLayout());

        // Create time label
        timeLabel = new JLabel("0:00:00.000", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(timeLabel, BorderLayout.CENTER);  // Add time label to the center

        // Button panel for Start, Stop, Reset
        JPanel buttonPanel = new JPanel(new GridLayout(1,3));  // Use FlowLayout for buttons
        add(buttonPanel, BorderLayout.SOUTH);  // Add the button panel to the bottom

        // Create buttons
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton resetButton = new JButton("Reset");

        // Add buttons to the button panel
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resetButton);

        // Timer: fires every 50ms to update milliseconds
        timer = new Timer(50, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (running)
                {
                    updateTimer();
                }
            }
        });

        // Button Actions
        startButton.addActionListener(e -> startTimer());
        stopButton.addActionListener(e -> stopTimer());
        resetButton.addActionListener(e -> resetTimer());
    }

    private void startTimer()
    {
        if (!running)
        {
            running = true;
            startTime = System.currentTimeMillis() - getElapsedTime();
            lastUpdateTime = startTime;  // Save the time at which the timer was started
            timer.start(); // Start the Swing Timer
        }
    }

    private void stopTimer()
    {
        running = false;
        timer.stop(); // Stop the Swing Timer
    }

    private void resetTimer()
    {
        stopTimer();
        startTime = 0;
        lastUpdateTime = 0;
        updateLabel(0); // Reset the label
    }

    private void updateTimer()
    {
        long elapsedTime = getElapsedTime();
        updateLabel(elapsedTime);
    }

    private long getElapsedTime()
    {
        if (startTime == 0)
        {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    private void updateLabel(long elapsedTime)
    {
        int hours = (int) (elapsedTime / 3600000);
        int minutes = (int) ((elapsedTime % 3600000) / 60000);
        int seconds = (int) ((elapsedTime % 60000) / 1000);
        int milliseconds = (int) (elapsedTime % 1000); // Get milliseconds

        String timeString = String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
        timeLabel.setText(timeString);

        // Refresh the panel
        revalidate();
        repaint();
    }
}
