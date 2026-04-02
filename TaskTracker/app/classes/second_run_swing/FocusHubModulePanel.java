package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.AppStats;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class FocusHubModulePanel extends JPanel {
    private AppStats appStats;

    // Timer Variables
    private Timer timer;
    private int timeLeft = 25 * 60;
    private boolean isFocusMode = true;

    // UI Components
    private JLabel timeDisplay;
    private JLabel statusLabel;
    private JButton startPauseBtn;
    private JComboBox<Integer> timerOptions;

    public FocusHubModulePanel(AppStats appStats) {
        this.appStats = appStats;
        setLayout(new GridLayout(1, 2, 20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- LEFT: Pomodoro Timer ---
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("FOCUS SESSION");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeDisplay = new JLabel("25:00");
        timeDisplay.setFont(new Font("Segoe UI", Font.BOLD, 120));
        timeDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Timer Dropdown
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        optionsPanel.add(new JLabel("Focus Length: "));
        timerOptions = new JComboBox<>(new Integer[]{10, 25, 30, 40, 60});
        timerOptions.setSelectedItem(25);
        optionsPanel.add(timerOptions);
        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerOptions.addActionListener(e -> {
            if (!timer.isRunning() && isFocusMode) {
                timeLeft = (Integer) timerOptions.getSelectedItem() * 60;
                updateDisplay();
            }
        });

        // Massive Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        startPauseBtn = new JButton("Start");
        startPauseBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        startPauseBtn.setPreferredSize(new Dimension(140, 50));

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        resetBtn.setPreferredSize(new Dimension(140, 50));

        btnPanel.add(startPauseBtn);
        btnPanel.add(resetBtn);

        timerPanel.add(Box.createVerticalGlue());
        timerPanel.add(statusLabel);
        timerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        timerPanel.add(optionsPanel);
        timerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        timerPanel.add(timeDisplay);
        timerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        timerPanel.add(btnPanel);
        timerPanel.add(Box.createVerticalGlue());

        setupTimerLogic();

        startPauseBtn.addActionListener(e -> {
            if (timer.isRunning()) { timer.stop(); startPauseBtn.setText("Resume"); }
            else { timer.start(); startPauseBtn.setText("Pause"); }
        });

        resetBtn.addActionListener(e -> resetTimer(isFocusMode));

        // --- RIGHT: Brain Dump Scratchpad ---
        JPanel scratchpadPanel = new JPanel(new BorderLayout(0, 10));
        JLabel scratchpadLabel = new JLabel("Brain Dump / Scratchpad");
        scratchpadLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scratchpadLabel.setForeground(Color.LIGHT_GRAY);

        JTextArea brainDumpArea = new JTextArea(appStats.getBrainDumpText());
        brainDumpArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        brainDumpArea.setLineWrap(true);
        brainDumpArea.setWrapStyleWord(true);
        brainDumpArea.setBackground(new Color(43, 45, 48));
        brainDumpArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        brainDumpArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStats(); }
            public void removeUpdate(DocumentEvent e) { updateStats(); }
            public void changedUpdate(DocumentEvent e) { updateStats(); }
            private void updateStats() { appStats.setBrainDumpText(brainDumpArea.getText()); }
        });

        JButton clearDumpBtn = new JButton("Clear Scratchpad");
        clearDumpBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearDumpBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear your notes?", "Clear Scratchpad", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                brainDumpArea.setText("");
            }
        });

        scratchpadPanel.add(scratchpadLabel, BorderLayout.NORTH);
        scratchpadPanel.add(new JScrollPane(brainDumpArea), BorderLayout.CENTER);
        scratchpadPanel.add(clearDumpBtn, BorderLayout.SOUTH);

        add(timerPanel);
        add(scratchpadPanel);
    }

    private void setupTimerLogic() {
        timer = new Timer(1000, e -> {
            timeLeft--;
            updateDisplay();
            if (timeLeft <= 0) {
                timer.stop();
                isFocusMode = !isFocusMode;
                resetTimer(isFocusMode);
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, isFocusMode ? "Break over! Back to work." : "Focus session complete. Take a break!", "Pomodoro", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void resetTimer(boolean focus) {
        timer.stop();
        isFocusMode = focus;
        timeLeft = isFocusMode ? (Integer) timerOptions.getSelectedItem() * 60 : 5 * 60;
        statusLabel.setText(isFocusMode ? "FOCUS SESSION" : "SHORT BREAK");
        statusLabel.setForeground(isFocusMode ? new Color(255, 100, 100) : new Color(46, 204, 113));
        startPauseBtn.setText("Start");
        updateDisplay();
    }

    private void updateDisplay() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timeDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }
}