package com.stopwatch;

import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.stopwatch.sound.SoundPlayer;

public class StopWatchPanel extends PluginPanel {
    private SoundPlayer soundPlayer;

    // Stopwatch variables
    private JLabel timeLabel;
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean stopwatchRunning = false;
    private Timer stopwatchTimer;

    // Countdown Timer variables
    private long countdownTime = 0;
    private boolean countdownRunning = false;
    private Timer countdownSwingTimer;
    private JLabel countdownLabel;

    public StopWatchPanel(StopWatchConfig config, SoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
        JTabbedPane tabbedPane = new JTabbedPane();

        // Stopwatch Tab
        JPanel stopWatchPanel = createStopWatchPanel();
        tabbedPane.addTab("Stopwatch", stopWatchPanel);

        // Countdown Timer Tab
        JPanel countdownPanel = createCountdownPanel(config);
        tabbedPane.addTab("Countdown Timer", countdownPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // Create Stopwatch Panel
    private JPanel createStopWatchPanel() {
        JPanel stopwatchPanel = new JPanel(new BorderLayout());

        // Stopwatch time label
        timeLabel = new JLabel("00:00.00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Top padding of 10 pixels
        stopwatchPanel.add(timeLabel, BorderLayout.NORTH);

        // Stopwatch Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton startStopButton = new JButton("Start");
        JButton resetButton = new JButton("Reset");

        // Set initial colors and add buttons to the panel
        startStopButton.setBackground(Color.decode("#397A66"));
        startStopButton.setForeground(Color.WHITE);
        buttonPanel.add(startStopButton);
        buttonPanel.add(resetButton);

        stopwatchPanel.add(buttonPanel, BorderLayout.CENTER);

        // Stopwatch Timer: fires every 50ms to update milliseconds
        stopwatchTimer = new Timer(50, e -> {
            if (stopwatchRunning) {
                updateStopwatch();
            }
        });

        // Start/Stop Button Action
        startStopButton.addActionListener(e -> {
            if (!stopwatchRunning) {
                startStopwatch();
                startStopButton.setText("Stop");
                startStopButton.setBackground(Color.decode("#C21614"));
            } else {
                stopStopwatch();
                startStopButton.setText("Start");
                startStopButton.setBackground(Color.decode("#397A66"));
            }
        });

        // Reset Button Action
        resetButton.addActionListener(e -> {
            resetStopwatch();
            startStopButton.setText("Start");
            startStopButton.setBackground(Color.decode("#397A66"));
        });

        return stopwatchPanel;
    }

    // Stopwatch Methods
    private void startStopwatch() {
        if (!stopwatchRunning) {
            stopwatchRunning = true;
            startTime = System.currentTimeMillis() - elapsedTime;
            stopwatchTimer.start();
        }
    }

    private void stopStopwatch() {
        if (stopwatchRunning) {
            elapsedTime = System.currentTimeMillis() - startTime; // Save the current elapsed time
            stopwatchRunning = false;
            stopwatchTimer.stop();
        }
    }

    private void resetStopwatch() {
        stopStopwatch();
        elapsedTime = 0;
        updateStopwatchLabel(0);
    }

    private void updateStopwatch() {
        updateStopwatchLabel(getElapsedStopwatchTime());
    }

    private long getElapsedStopwatchTime() {
        if (!stopwatchRunning) {
            return elapsedTime; // Return stored elapsed time when not running
        }
        return System.currentTimeMillis() - startTime; // Calculate elapsed time when running
    }

    private void updateStopwatchLabel(long elapsedTime) {
        // int hours = (int) (elapsedTime / 3600000);
        int minutes = (int) ((elapsedTime % 3600000) / 60000);
        int seconds = (int) ((elapsedTime % 60000) / 1000);
        int centiseconds = (int) ((elapsedTime % 1000) / 10);

        String timeString = String.format("%02d:%02d.%02d", minutes, seconds, centiseconds);
        timeLabel.setText(timeString);
    }

    // Create Countdown Panel
    private JPanel createCountdownPanel(StopWatchConfig config) {
        JPanel countdownPanel = new JPanel(new BorderLayout());
        JPanel countdownTimerPanel = new JPanel(new BorderLayout());

        // Countdown time display
        countdownLabel = new JLabel("00:00.00", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 40));
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Top padding of 10 pixels
        countdownTimerPanel.add(countdownLabel, BorderLayout.NORTH);

        // Start/Cancel Button
        JButton startCancelButton = new JButton("Start Countdown");
        startCancelButton.setBackground(Color.decode("#397A66")); // Initial greenish color
        startCancelButton.setForeground(Color.WHITE);
        startCancelButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        countdownTimerPanel.add(startCancelButton, BorderLayout.SOUTH);
        countdownPanel.add(countdownTimerPanel, BorderLayout.NORTH);

        // Create a separator (horizontal line)
        JSeparator separatorTop = new JSeparator(SwingConstants.HORIZONTAL);

        // Input Textbox
        JTextField timeInputField = new JTextField("00:30", 10); // Placeholder
        timeInputField.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a panel for the input field
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(separatorTop);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel setTimerLabel = new JLabel("Set Timer");
        setTimerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(setTimerLabel);
        centerPanel.add(timeInputField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Add this new centerPanel to the main panel
        countdownPanel.add(centerPanel, BorderLayout.CENTER);  // Add the centerPanel to the main panel

        // Create a separator (horizontal line)
        JSeparator separatorBottom = new JSeparator(SwingConstants.HORIZONTAL);

        // Preset Buttons Panel
        JLabel presetPanelLabel = new JLabel("Timer Presets");
        presetPanelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel presetPanel = new JPanel();
        presetPanel.setLayout(new BoxLayout(presetPanel, BoxLayout.Y_AXIS));
        JButton cerberusPresetButton = new JButton("Cerberus");
        JButton inferno1PresetButton = new JButton("Inferno 1st Set");
        JButton inferno2PresetButton = new JButton("Inferno 2nd Set");

        JPanel presetButtonPanel = new JPanel(new GridLayout(3, 1));
        presetButtonPanel.add(cerberusPresetButton);
        presetButtonPanel.add(inferno1PresetButton);
        presetButtonPanel.add(inferno2PresetButton);

        // Adding preset buttons
        presetPanel.add(separatorBottom);
        presetPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        presetPanel.add(presetPanelLabel);
        presetPanel.add(presetButtonPanel);
        countdownPanel.add(presetPanel, BorderLayout.SOUTH);

        // Countdown Timer Variables
        countdownSwingTimer = new Timer(50, null); // 50ms update
        countdownSwingTimer.setRepeats(true);
        long[] selectedCountdownTime = {0}; // Store original countdown time

        // Define the countdown action listener
        final ActionListener countdownAction = e -> {
            if (countdownTime > 0) {
                countdownTime -= 50; // Decrement by 50ms
                updateCountdownDisplay(countdownTime);
            } else {
                // Countdown reached 0
                countdownSwingTimer.stop();
                countdownRunning = false;

                if (config.useSound()) {
                    this.soundPlayer.playSound(); // Play sound on completion
                }

                // Reset the button to its initial state
                startCancelButton.setText("Start Countdown");
                startCancelButton.setBackground(Color.decode("#397A66"));
            }
        };

        // Button Behavior
        startCancelButton.addActionListener(e -> {
            if (!countdownRunning) {
                // Start Countdown
                String userInput = timeInputField.getText().trim();
                try {
                    long totalMilliseconds = parseInputToMilliseconds(userInput);
                    selectedCountdownTime[0] = totalMilliseconds; // Store original
                    startCountdown(totalMilliseconds);
                    countdownSwingTimer.stop(); // Clear previous runs
                    for (ActionListener listener : countdownSwingTimer.getActionListeners()) {
                        countdownSwingTimer.removeActionListener(listener);
                    }
                    countdownSwingTimer.addActionListener(countdownAction); // Add listener once
                    countdownSwingTimer.start();

                    startCancelButton.setText("Cancel");
                    startCancelButton.setBackground(Color.decode("#C21614")); // Red color
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(countdownPanel, "Invalid time format. Use mm:ss.");
                }
            } else {
                // Cancel Countdown
                countdownSwingTimer.stop();
                countdownSwingTimer.removeActionListener(countdownAction); // Remove listener
                countdownTime = selectedCountdownTime[0];
                updateCountdownDisplay(countdownTime);
                countdownRunning = false;

                startCancelButton.setText("Start Countdown");
                startCancelButton.setBackground(Color.decode("#397A66")); // Greenish color
            }
        });

        // Preset Buttons Behavior
        cerberusPresetButton.addActionListener(e -> {
            timeInputField.setText("0:56"); // Set Cerberus preset
            updateCountdownPreset(timeInputField);
        });

        inferno1PresetButton.addActionListener(e -> {
            timeInputField.setText("3:30"); // Set Inferno 1st Set preset
            updateCountdownPreset(timeInputField);
        });

        inferno2PresetButton.addActionListener(e -> {
            timeInputField.setText("5:15"); // Set Inferno 2nd Set preset
            updateCountdownPreset(timeInputField);
        });

        // Add DocumentListener to monitor text changes
        timeInputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTimer();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTimer();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTimer();
            }

            private void updateTimer() {
                updateCountdownPreset(timeInputField);
            }
        });

        return countdownPanel;
    }

    private void updateCountdownPreset(JTextField timeInputField) {
        if (!countdownRunning) {
            String userInput = timeInputField.getText().trim();
            try {
                long totalMilliseconds = parseInputToMilliseconds(userInput);
                updateCountdownDisplay(totalMilliseconds);
            } catch (NumberFormatException ex) {
                // System.err.println("Error updating countdown timer: " + ex.getMessage());
            }
        }
    }

    // Parses user input in "mm:ss" format into milliseconds
    private long parseInputToMilliseconds(String input) throws NumberFormatException {
        String[] parts = input.split(":");
        if (parts.length != 2) {
            throw new NumberFormatException("Invalid format");
        }
        int minutes = Integer.parseInt(parts[0].trim());
        int seconds = Integer.parseInt(parts[1].trim());
        return (minutes * 60L + seconds) * 1000L; // Convert to milliseconds
    }

    // Starts the countdown
    private void startCountdown(long totalMilliseconds) {
        countdownTime = totalMilliseconds;
        countdownRunning = true;
    }

    // Updates the countdown display with milliseconds
    private void updateCountdownDisplay(long timeInMilliseconds) {
        int minutes = (int) (timeInMilliseconds / 60000);
        int seconds = (int) ((timeInMilliseconds % 60000) / 1000);
        int centiseconds = (int) ((timeInMilliseconds % 1000) / 10);
        countdownLabel.setText(String.format("%02d:%02d.%02d", minutes, seconds, centiseconds));
    }
}
