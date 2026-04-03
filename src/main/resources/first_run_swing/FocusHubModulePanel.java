package com.raeden.ors_to_do;

import com.raeden.ors_to_do.dependencies.models.AppStats;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class FocusHubModulePanel extends JPanel {
    private AppStats appStats;

    // Timer Variables
    private Timer timer;
    private int timeLeft = 25 * 60; // 25 minutes in seconds
    private boolean isFocusMode = true;

    // UI Components
    private JLabel timeDisplay;
    private JLabel statusLabel;
    private JButton startPauseBtn;

    public FocusHubModulePanel(AppStats appStats) {
        this.appStats = appStats;
        setLayout(new GridLayout(1, 2, 20, 0)); // Split Left/Right
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- LEFT: Pomodoro Timer ---
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("FOCUS SESSION");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        statusLabel.setForeground(new Color(255, 100, 100));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeDisplay = new JLabel("25:00");
        timeDisplay.setFont(new Font("Segoe UI", Font.BOLD, 120)); // Massive font
        timeDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        startPauseBtn = new JButton("Start");
        JButton resetBtn = new JButton("Reset");

        btnPanel.add(startPauseBtn);
        btnPanel.add(resetBtn);

        timerPanel.add(Box.createVerticalGlue());
        timerPanel.add(statusLabel);
        timerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
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
        brainDumpArea.setBackground(new Color(43, 45, 48)); // Dark editor feel
        brainDumpArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Auto-save logic (Updates the object in memory on every keystroke)
        brainDumpArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStats(); }
            public void removeUpdate(DocumentEvent e) { updateStats(); }
            public void changedUpdate(DocumentEvent e) { updateStats(); }
            private void updateStats() { appStats.setBrainDumpText(brainDumpArea.getText()); }
        });

        scratchpadPanel.add(scratchpadLabel, BorderLayout.NORTH);
        scratchpadPanel.add(new JScrollPane(brainDumpArea), BorderLayout.CENTER);

        // --- Assemble ---
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
                Toolkit.getDefaultToolkit().beep(); // Alert sound
                JOptionPane.showMessageDialog(this, isFocusMode ? "Break over! Back to work." : "Focus session complete. Take a break!", "Pomodoro", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void resetTimer(boolean focus) {
        timer.stop();
        isFocusMode = focus;
        timeLeft = isFocusMode ? 25 * 60 : 5 * 60;
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