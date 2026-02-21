package com.stopwatch;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import com.stopwatch.sound.SoundPlayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class StopWatchPanel extends PluginPanel {

    private static final int MAX_CUSTOM_PRESETS = 10;

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color BG_CARD          = new Color(45, 45, 45);
    private static final Color BG_MEDIUM        = new Color(40, 40, 40);
    private static final Color BG_INPUT         = new Color(35, 35, 35);
    private static final Color ACCENT_GREEN      = new Color(57, 122, 102);
    private static final Color ACCENT_GREEN_HOV  = new Color(70, 145, 122);
    private static final Color ACCENT_RED        = new Color(194, 22, 20);
    private static final Color ACCENT_RED_HOV    = new Color(220, 50, 48);
    private static final Color ACCENT_AMBER      = new Color(200, 155, 60);
    private static final Color TEXT_PRIMARY      = new Color(220, 210, 190);
    private static final Color TEXT_SECONDARY    = new Color(150, 140, 125);
    private static final Color TEXT_MUTED        = new Color(100, 95, 85);
    private static final Color BORDER_COLOR      = new Color(60, 58, 54);
    private static final Color SEPARATOR_COLOR   = new Color(55, 53, 50);

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final SoundPlayer soundPlayer;
    private final StopWatchConfig config;
    private final ConfigManager configManager;

    // ── Stopwatch state ───────────────────────────────────────────────────────
    private JLabel timeLabel;
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean stopwatchRunning = false;
    private Timer stopwatchTimer;

    // ── Countdown state ───────────────────────────────────────────────────────
    private long countdownTime = 0;
    private boolean countdownRunning = false;
    private Timer countdownSwingTimer;
    private JLabel countdownLabel;

    // ── Countdown inputs (separate min/sec spinners) ──────────────────────────
    private JSpinner minSpinner;
    private JSpinner secSpinner;

    // ── Custom presets ────────────────────────────────────────────────────────
    private final List<CustomPreset> customPresets = new ArrayList<>();
    private JPanel customPresetsContainer;
    private JLabel addPresetHint; // shows "Max 10 reached" when full

    // ── Config keys ───────────────────────────────────────────────────────────
    private static final String CONFIG_GROUP       = "stopwatch";
    private static final String KEY_LAST_MIN       = "lastTimerMin";
    private static final String KEY_LAST_SEC       = "lastTimerSec";
    private static final String KEY_CUSTOM_PRESETS = "customPresets";

    // ─────────────────────────────────────────────────────────────────────────

    public StopWatchPanel(StopWatchConfig config, SoundPlayer soundPlayer, ConfigManager configManager) {
        this.config = config;
        this.soundPlayer = soundPlayer;
        this.configManager = configManager;

        loadCustomPresets();

        // Do NOT set a background — keep the default RuneLite panel colour
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(FontManager.getRunescapeSmallFont());
        tabs.setFocusable(false);
        tabs.addTab("Stopwatch", buildStopwatchTab());
        tabs.addTab("Countdown", buildCountdownTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Stopwatch tab
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildStopwatchTab() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(12, 8, 8, 8));

        // Timer display
        timeLabel = new JLabel("00:00.00", SwingConstants.CENTER);
        timeLabel.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD, 40f));
        timeLabel.setForeground(TEXT_PRIMARY);
        timeLabel.setAlignmentX(LEFT_ALIGNMENT);
        timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        timeLabel.setBorder(new EmptyBorder(4, 0, 8, 0));
        root.add(timeLabel);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JButton startStopBtn = buildPrimaryButton("Start", ACCENT_GREEN, ACCENT_GREEN_HOV);
        JButton resetBtn     = buildSecondaryButton("Reset");
        btnRow.add(startStopBtn);
        btnRow.add(resetBtn);
        root.add(btnRow);

        stopwatchTimer = new Timer(50, e -> { if (stopwatchRunning) updateStopwatchDisplay(); });

        startStopBtn.addActionListener(e -> {
            if (!stopwatchRunning) {
                startStopwatch();
                startStopBtn.setText("Stop");
                applyButtonColor(startStopBtn, ACCENT_RED, ACCENT_RED_HOV);
            } else {
                stopStopwatch();
                startStopBtn.setText("Start");
                applyButtonColor(startStopBtn, ACCENT_GREEN, ACCENT_GREEN_HOV);
            }
        });

        resetBtn.addActionListener(e -> {
            resetStopwatch();
            startStopBtn.setText("Start");
            applyButtonColor(startStopBtn, ACCENT_GREEN, ACCENT_GREEN_HOV);
        });

        return root;
    }

    private void startStopwatch() {
        if (!stopwatchRunning) {
            stopwatchRunning = true;
            startTime = System.currentTimeMillis() - elapsedTime;
            stopwatchTimer.start();
        }
    }

    private void stopStopwatch() {
        if (stopwatchRunning) {
            elapsedTime = System.currentTimeMillis() - startTime;
            stopwatchRunning = false;
            stopwatchTimer.stop();
        }
    }

    private void resetStopwatch() {
        stopStopwatch();
        elapsedTime = 0;
        updateStopwatchLabel(0);
    }

    private void updateStopwatchDisplay() {
        updateStopwatchLabel(stopwatchRunning ? System.currentTimeMillis() - startTime : elapsedTime);
    }

    private void updateStopwatchLabel(long ms) {
        timeLabel.setText(String.format("%02d:%02d.%02d",
                (int) ((ms % 3_600_000) / 60_000),
                (int) ((ms % 60_000) / 1_000),
                (int) ((ms % 1_000) / 10)));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Countdown tab
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildCountdownTab() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(12, 8, 8, 8));

        // ── Countdown display ─────────────────────────────────────────────────
        countdownLabel = new JLabel("00:00.00", SwingConstants.CENTER);
        countdownLabel.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD, 40f));
        countdownLabel.setForeground(TEXT_PRIMARY);
        countdownLabel.setAlignmentX(LEFT_ALIGNMENT);
        countdownLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        countdownLabel.setBorder(new EmptyBorder(4, 0, 8, 0));
        root.add(countdownLabel);

        // ── Min / Sec spinners ────────────────────────────────────────────────
        int savedMin = loadInt(KEY_LAST_MIN, 0);
        int savedSec = loadInt(KEY_LAST_SEC, 30);

        minSpinner = buildSpinner(savedMin, 0, 99);
        secSpinner = buildSpinner(savedSec, 0, 59);

        // Update display whenever spinners change, and persist
        minSpinner.addChangeListener(e -> onSpinnerChanged());
        secSpinner.addChangeListener(e -> onSpinnerChanged());

        // Each spinner gets half the available width; labels are fixed-width
        JPanel minGroup = new JPanel(new BorderLayout(4, 0));
        minGroup.setOpaque(false);
        minGroup.add(buildLabel("min", TEXT_SECONDARY), BorderLayout.WEST);
        minGroup.add(minSpinner, BorderLayout.CENTER);

        JPanel secGroup = new JPanel(new BorderLayout(4, 0));
        secGroup.setOpaque(false);
        secGroup.add(buildLabel("sec", TEXT_SECONDARY), BorderLayout.WEST);
        secGroup.add(secSpinner, BorderLayout.CENTER);

        JPanel spinnerRow = new JPanel(new GridLayout(1, 2, 8, 0));
        spinnerRow.setOpaque(false);
        spinnerRow.setAlignmentX(LEFT_ALIGNMENT);
        spinnerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        spinnerRow.add(minGroup);
        spinnerRow.add(secGroup);

        root.add(spinnerRow);
        root.add(rigidV(8));

        // Initialise display
        updateCountdownDisplay(spinnerToMs());

        // ── Start / Cancel button ─────────────────────────────────────────────
        JButton startCancelBtn = buildPrimaryButton("Start Countdown", ACCENT_GREEN, ACCENT_GREEN_HOV);
        startCancelBtn.setAlignmentX(LEFT_ALIGNMENT);
        startCancelBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        root.add(startCancelBtn);
        root.add(rigidV(14));

        // ── Countdown timer logic ─────────────────────────────────────────────
        countdownSwingTimer = new Timer(50, null);
        countdownSwingTimer.setRepeats(true);
        long[] originalTime = {0};

        final ActionListener countdownTick = e -> {
            if (countdownTime > 0) {
                countdownTime -= 50;
                updateCountdownDisplay(countdownTime);
            } else {
                countdownSwingTimer.stop();
                countdownRunning = false;
                if (config.useSound()) soundPlayer.playSound();
                startCancelBtn.setText("Start Countdown");
                applyButtonColor(startCancelBtn, ACCENT_GREEN, ACCENT_GREEN_HOV);
            }
        };

        startCancelBtn.addActionListener(e -> {
            if (!countdownRunning) {
                long ms = spinnerToMs();
                if (ms == 0) {
                    JOptionPane.showMessageDialog(this, "Please set a time greater than 0.");
                    return;
                }
                originalTime[0] = ms;
                countdownTime = ms;
                countdownRunning = true;
                for (ActionListener l : countdownSwingTimer.getActionListeners())
                    countdownSwingTimer.removeActionListener(l);
                countdownSwingTimer.addActionListener(countdownTick);
                countdownSwingTimer.start();
                startCancelBtn.setText("Cancel");
                applyButtonColor(startCancelBtn, ACCENT_RED, ACCENT_RED_HOV);
            } else {
                countdownSwingTimer.stop();
                for (ActionListener l : countdownSwingTimer.getActionListeners())
                    countdownSwingTimer.removeActionListener(l);
                countdownRunning = false;
                countdownTime = originalTime[0];
                updateCountdownDisplay(countdownTime);
                startCancelBtn.setText("Start Countdown");
                applyButtonColor(startCancelBtn, ACCENT_GREEN, ACCENT_GREEN_HOV);
            }
        });

        // ── Built-in presets ──────────────────────────────────────────────────
        // Layout: row 1 = Cerberus | Birdhouse Run
        //         row 2 = Inferno 1st | Inferno 2nd
        root.add(buildSectionDivider("Built-in Presets"));
        root.add(rigidV(6));

        JPanel builtinWrapper = new JPanel(new BorderLayout());
        builtinWrapper.setOpaque(false);
        builtinWrapper.setAlignmentX(LEFT_ALIGNMENT);

        JPanel builtinGrid = new JPanel(new GridLayout(0, 2, 5, 5));
        builtinGrid.setOpaque(false);
        addBuiltinPreset(builtinGrid, "Cerberus",      0, 56);
        addBuiltinPreset(builtinGrid, "Birdhouse Run", 50, 0);
        addBuiltinPreset(builtinGrid, "Inferno 1st",   3, 30);
        addBuiltinPreset(builtinGrid, "Inferno 2nd",   5, 15);

        builtinWrapper.add(builtinGrid, BorderLayout.CENTER);
        root.add(builtinWrapper);
        root.add(rigidV(14));

        // ── Custom presets ────────────────────────────────────────────────────
        root.add(buildSectionDivider("Custom Presets"));
        root.add(rigidV(6));

        JPanel addRow = buildAddPresetRow();
        addRow.setAlignmentX(LEFT_ALIGNMENT);
        root.add(addRow);
        root.add(rigidV(4));

        // Hint label shown when limit is reached
        addPresetHint = new JLabel("Max " + MAX_CUSTOM_PRESETS + " presets reached.");
        addPresetHint.setForeground(TEXT_MUTED);
        addPresetHint.setFont(FontManager.getRunescapeSmallFont());
        addPresetHint.setAlignmentX(LEFT_ALIGNMENT);
        addPresetHint.setVisible(false);
        root.add(addPresetHint);
        root.add(rigidV(4));

        customPresetsContainer = new JPanel();
        customPresetsContainer.setLayout(new BoxLayout(customPresetsContainer, BoxLayout.Y_AXIS));
        customPresetsContainer.setOpaque(false);
        customPresetsContainer.setAlignmentX(LEFT_ALIGNMENT);
        root.add(customPresetsContainer);

        refreshCustomPresetsUI();

        return root;
    }

    /** Called whenever a spinner value changes — updates display and persists. */
    private void onSpinnerChanged() {
        if (!countdownRunning) {
            updateCountdownDisplay(spinnerToMs());
            configManager.setConfiguration(CONFIG_GROUP, KEY_LAST_MIN, String.valueOf((int) minSpinner.getValue()));
            configManager.setConfiguration(CONFIG_GROUP, KEY_LAST_SEC, String.valueOf((int) secSpinner.getValue()));
        }
    }

    /** Sets the spinners to a given minutes + seconds value (used by presets). */
    private void setSpinners(int minutes, int seconds) {
        minSpinner.setValue(minutes);
        secSpinner.setValue(seconds);
    }

    /** Reads current spinner values and converts to milliseconds. */
    private long spinnerToMs() {
        int min = (int) minSpinner.getValue();
        int sec = (int) secSpinner.getValue();
        return (min * 60L + sec) * 1_000L;
    }

    // ── Add-preset form ───────────────────────────────────────────────────────

    private JPanel buildAddPresetRow() {
        // Two-line layout:
        //   Line 1: [ Preset name field (full width) ]
        //   Line 2: [ min group | sec group | + button ]
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);

        // Line 1 - name field
        JTextField nameField = buildStyledTextField("Preset name");
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        wrapper.add(nameField);
        wrapper.add(rigidV(4));

        // Line 2 - spinners + add button
        JSpinner pMin = buildSpinner(0, 0, 99);
        JSpinner pSec = buildSpinner(0, 0, 59);

        JPanel minGroup = new JPanel(new BorderLayout(4, 0));
        minGroup.setOpaque(false);
        minGroup.add(buildLabel("min", TEXT_SECONDARY), BorderLayout.WEST);
        minGroup.add(pMin, BorderLayout.CENTER);

        JPanel secGroup = new JPanel(new BorderLayout(4, 0));
        secGroup.setOpaque(false);
        secGroup.add(buildLabel("sec", TEXT_SECONDARY), BorderLayout.WEST);
        secGroup.add(pSec, BorderLayout.CENTER);

        JPanel spinners = new JPanel(new GridLayout(1, 2, 6, 0));
        spinners.setOpaque(false);
        spinners.add(minGroup);
        spinners.add(secGroup);

        JButton addBtn = buildPrimaryButton("+", ACCENT_GREEN, ACCENT_GREEN_HOV);
        addBtn.setPreferredSize(new Dimension(28, 26));
        addBtn.setToolTipText("Add preset");

        JPanel timeLine = new JPanel(new BorderLayout(6, 0));
        timeLine.setOpaque(false);
        timeLine.setAlignmentX(LEFT_ALIGNMENT);
        timeLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        timeLine.add(spinners, BorderLayout.CENTER);
        timeLine.add(addBtn,   BorderLayout.EAST);
        wrapper.add(timeLine);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty() || name.equals("Preset name")) {
                JOptionPane.showMessageDialog(this, "Please enter a preset name.");
                return;
            }
            if (customPresets.size() >= MAX_CUSTOM_PRESETS) {
                JOptionPane.showMessageDialog(this, "Maximum of " + MAX_CUSTOM_PRESETS + " presets reached.");
                return;
            }
            int min = (int) pMin.getValue();
            int sec = (int) pSec.getValue();
            if (min == 0 && sec == 0) {
                JOptionPane.showMessageDialog(this, "Please set a time greater than 0.");
                return;
            }
            customPresets.add(new CustomPreset(name, min, sec));
            saveCustomPresets();
            refreshCustomPresetsUI();
            nameField.setText("Preset name");
            nameField.setForeground(TEXT_SECONDARY);
            pMin.setValue(0);
            pSec.setValue(0);
        });

        return wrapper;
    }

    // ── Custom preset list ────────────────────────────────────────────────────

    private void refreshCustomPresetsUI() {
        customPresetsContainer.removeAll();

        boolean atLimit = customPresets.size() >= MAX_CUSTOM_PRESETS;
        if (addPresetHint != null) addPresetHint.setVisible(atLimit);

        if (customPresets.isEmpty()) {
            JLabel empty = new JLabel("No custom presets yet.");
            empty.setForeground(TEXT_MUTED);
            empty.setFont(FontManager.getRunescapeSmallFont());
            empty.setBorder(new EmptyBorder(2, 2, 4, 0));
            empty.setAlignmentX(LEFT_ALIGNMENT);
            customPresetsContainer.add(empty);
        } else {
            for (int i = 0; i < customPresets.size(); i++) {
                JPanel row = buildCustomPresetRow(i);
                row.setAlignmentX(LEFT_ALIGNMENT);
                customPresetsContainer.add(row);
                customPresetsContainer.add(rigidV(4));
            }
        }

        customPresetsContainer.revalidate();
        customPresetsContainer.repaint();
    }

    private JPanel buildCustomPresetRow(int index) {
        CustomPreset preset = customPresets.get(index);

        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setBackground(BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(4, 6, 4, 4)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel nameLabel = new JLabel(preset.name);
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setFont(FontManager.getRunescapeSmallFont());

        JLabel timeTag = new JLabel(preset.displayTime());
        timeTag.setForeground(TEXT_SECONDARY);
        timeTag.setFont(FontManager.getRunescapeSmallFont());
        timeTag.setBorder(new EmptyBorder(0, 0, 0, 4));

        JButton deleteBtn = new JButton("x");
        deleteBtn.setFont(FontManager.getRunescapeSmallFont());
        deleteBtn.setBackground(new Color(80, 40, 40));
        deleteBtn.setForeground(TEXT_SECONDARY);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.setToolTipText("Remove");
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { deleteBtn.setBackground(ACCENT_RED); deleteBtn.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { deleteBtn.setBackground(new Color(80, 40, 40)); deleteBtn.setForeground(TEXT_SECONDARY); }
        });
        deleteBtn.addActionListener(e -> {
            customPresets.remove(index);
            saveCustomPresets();
            refreshCustomPresetsUI();
        });

        MouseAdapter applyClick = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { setSpinners(preset.minutes, preset.seconds); }
            @Override public void mouseEntered(MouseEvent e) {
                row.setBackground(BG_MEDIUM);
                nameLabel.setForeground(ACCENT_AMBER);
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override public void mouseExited(MouseEvent e) {
                row.setBackground(BG_CARD);
                nameLabel.setForeground(TEXT_PRIMARY);
                row.setCursor(Cursor.getDefaultCursor());
            }
        };
        row.addMouseListener(applyClick);
        nameLabel.addMouseListener(applyClick);
        timeTag.addMouseListener(applyClick);

        row.add(nameLabel, BorderLayout.CENTER);
        row.add(timeTag,   BorderLayout.WEST);
        row.add(deleteBtn, BorderLayout.EAST);
        return row;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Persistence
    // ══════════════════════════════════════════════════════════════════════════

    /** Stored as "Name|min|sec|Name2|min2|sec2|..." */
    private void saveCustomPresets() {
        if (customPresets.isEmpty()) {
            configManager.setConfiguration(CONFIG_GROUP, KEY_CUSTOM_PRESETS, "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (CustomPreset p : customPresets) {
            if (sb.length() > 0) sb.append("|");
            sb.append(p.name.replace("|", ""))
                    .append("|").append(p.minutes)
                    .append("|").append(p.seconds);
        }
        configManager.setConfiguration(CONFIG_GROUP, KEY_CUSTOM_PRESETS, sb.toString());
    }

    private void loadCustomPresets() {
        customPresets.clear();
        String raw = configManager.getConfiguration(CONFIG_GROUP, KEY_CUSTOM_PRESETS);
        if (raw == null || raw.isEmpty()) return;
        String[] parts = raw.split("\\|");
        for (int i = 0; i + 2 < parts.length; i += 3) {
            try {
                String name = parts[i].trim();
                int min = Integer.parseInt(parts[i + 1].trim());
                int sec = Integer.parseInt(parts[i + 2].trim());
                if (!name.isEmpty()) customPresets.add(new CustomPreset(name, min, sec));
            } catch (NumberFormatException ignored) {}
        }
    }

    private int loadInt(String key, int defaultVal) {
        String raw = configManager.getConfiguration(CONFIG_GROUP, key);
        if (raw == null || raw.isEmpty()) return defaultVal;
        try { return Integer.parseInt(raw.trim()); } catch (NumberFormatException e) { return defaultVal; }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════════════════════

    private void addBuiltinPreset(JPanel grid, String label, int minutes, int seconds) {
        JButton btn = new JButton(label);
        btn.setFont(FontManager.getRunescapeSmallFont());
        btn.setBackground(BG_CARD);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(5, 4, 5, 4)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(String.format("%dm %ds", minutes, seconds));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BG_MEDIUM); btn.setForeground(ACCENT_AMBER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(BG_CARD);   btn.setForeground(TEXT_SECONDARY); }
        });
        btn.addActionListener(e -> setSpinners(minutes, seconds));
        grid.add(btn);
    }

    private JSpinner buildSpinner(int value, int min, int max) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        s.setFont(FontManager.getRunescapeSmallFont());
        // Make the text field inside the spinner styled consistently
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(s, "00");
        s.setEditor(editor);
        editor.getTextField().setBackground(BG_INPUT);
        editor.getTextField().setForeground(TEXT_PRIMARY);
        editor.getTextField().setCaretColor(TEXT_PRIMARY);
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
        editor.getTextField().setBorder(new EmptyBorder(2, 2, 2, 2));
        s.setBorder(new MatteBorder(1, 1, 1, 1, BORDER_COLOR));
        return s;
    }

    private JPanel buildSectionDivider(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JLabel lbl = new JLabel(title.toUpperCase());
        lbl.setFont(FontManager.getRunescapeSmallFont());
        lbl.setForeground(TEXT_MUTED);

        JSeparator sep = new JSeparator();
        sep.setForeground(SEPARATOR_COLOR);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;
        gc.gridx = 0; gc.weightx = 0; gc.insets = new Insets(0, 0, 0, 6);
        p.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);
        p.add(sep, gc);

        return p;
    }

    private JLabel buildLabel(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(FontManager.getRunescapeSmallFont());
        l.setForeground(color);
        return l;
    }

    private JTextField buildStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_SECONDARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setFont(FontManager.getRunescapeSmallFont());
        f.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(3, 5, 3, 5)));
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_SECONDARY); }
            }
        });
        return f;
    }

    private JButton buildPrimaryButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(FontManager.getRunescapeSmallFont());
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FontManager.getRunescapeSmallFont());
        btn.setBackground(BG_CARD);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BG_MEDIUM); btn.setForeground(TEXT_PRIMARY); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(BG_CARD);   btn.setForeground(TEXT_SECONDARY); }
        });
        return btn;
    }

    private void applyButtonColor(JButton btn, Color bg, Color hover) {
        btn.setBackground(bg);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
    }

    private void updateCountdownDisplay(long ms) {
        countdownLabel.setText(String.format("%02d:%02d.%02d",
                (int) (ms / 60_000),
                (int) ((ms % 60_000) / 1_000),
                (int) ((ms % 1_000) / 10)));
    }

    private static Component rigidV(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Inner types
    // ══════════════════════════════════════════════════════════════════════════

    private static class CustomPreset {
        final String name;
        final int minutes;
        final int seconds;

        CustomPreset(String name, int minutes, int seconds) {
            this.name = name;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        String displayTime() {
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable onChange;
        SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { onChange.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { onChange.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }
}