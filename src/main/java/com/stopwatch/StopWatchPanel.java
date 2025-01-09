package com.stopwatch;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopWatchPanel extends PluginPanel
{
    private final JLabel timerLabel;
    private final Timer timer;

    private long startTime = 0;
    private boolean running = false;

    public StopWatchPanel()
    {
        setLayout(new BorderLayout());
        timerLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Timer to update the stopwatch
        timer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (running)
                {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    timerLabel.setText(formatTime(elapsedTime));
                }
            }
        });

        add(timerLabel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    public void init()
    {
        timer.start();
    }

    private JPanel createButtonPanel()
    {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

        // Start Button
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startTimer());
        buttonPanel.add(startButton);

        // Stop Button
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopTimer());
        buttonPanel.add(stopButton);

        // Reset Button
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetTimer());
        buttonPanel.add(resetButton);

        return buttonPanel;
    }

    private void startTimer()
    {
        if (!running)
        {
            running = true;
            startTime = System.currentTimeMillis();
        }
    }

    private void stopTimer()
    {
        running = false;
    }

    private void resetTimer()
    {
        running = false;
        startTime = 0;
        timerLabel.setText("00:00:00");
    }

    private String formatTime(long millis)
    {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours = millis / (1000 * 60 * 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}