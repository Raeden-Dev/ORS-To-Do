package com.raeden.ors_to_do;

import com.formdev.flatlaf.FlatDarkLaf;
import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;

public class TaskTrackerApp extends JFrame {

    // --- Data Models ---
    private List<TaskItem> taskDatabase;
    private AppStats appStats; // FIXED: Added missing AppStats declaration

    // --- UI Components ---
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // --- Module Panels ---
    private TaskModulePanel quickToDoPanel;
    private DailyModulePanel dailyToDoPanel; // FIXED: Added missing DailyModulePanel declaration
    private TaskModulePanel workListPanel;
    private ArchivedModulePanel archivedPanel;
    private FocusHubModulePanel focusHubPanel;

    public TaskTrackerApp() {
        // 1. Load Data
        taskDatabase = StorageManager.loadTasks();
        appStats = StorageManager.loadStats();

        // 2. Setup JFrame
        setTitle("Task-Tracker");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 3. Process Data
        processDraftsOnLoad();
        processDailyRollover(taskDatabase, appStats);

        // 4. Initialize Components
        initUI();

        // 5. Add Window Listener for Saving
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveDraftsOnClose();
                autoArchiveTasks();
                StorageManager.saveTasks(taskDatabase);
                StorageManager.saveStats(appStats);
            }
        });
    }

    private void initUI() {
        // --- SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        Runnable syncUI = () -> {
            if (quickToDoPanel != null) quickToDoPanel.refreshList();
            // Optional: sync daily list if you ever implement cross-tab moving for it
            if (workListPanel != null) workListPanel.refreshList();
            if (archivedPanel != null) archivedPanel.refreshList();
        };

        // --- MAIN CONTENT (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // Define Module Names
        String[] modules = {"Quick To-Do", "Daily To-Do", "Work List", "Focus Hub", "Archived"};

        // Populate Sidebar Buttons and Module Panels
        for (String moduleName : modules) {

            // Create navigation button
            JButton navBtn = new JButton(moduleName);
            navBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            navBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            navBtn.setFocusPainted(false);
            navBtn.addActionListener(e -> cardLayout.show(mainContentPanel, moduleName));

            sidebar.add(navBtn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing

            // FIXED: Added proper instantiation for the Daily To-Do panel
            JPanel modulePanel;
            if (moduleName.equals("Quick To-Do")) {
                quickToDoPanel = new TaskModulePanel(TaskItem.OriginModule.QUICK, taskDatabase);
                modulePanel = quickToDoPanel;
            } else if (moduleName.equals("Daily To-Do")) {
                dailyToDoPanel = new DailyModulePanel(taskDatabase, appStats);
                modulePanel = dailyToDoPanel;
            } else if (moduleName.equals("Work List")) {
                workListPanel = new TaskModulePanel(TaskItem.OriginModule.WORK, taskDatabase);
                modulePanel = workListPanel;
            } else if (moduleName.equals("Focus Hub")) {
                focusHubPanel = new FocusHubModulePanel(appStats);
                modulePanel = focusHubPanel;
            } else if (moduleName.equals("Archived")) {
                archivedPanel = new ArchivedModulePanel(taskDatabase, syncUI);
                modulePanel = archivedPanel;
            } else {
                modulePanel = createPlaceholderPanel(moduleName);
            }

            mainContentPanel.add(modulePanel, moduleName);
        }

        // Add to Frame
        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void processDraftsOnLoad() {
        var drafts = appStats.getPendingDrafts();
        if (drafts == null || drafts.isEmpty()) return;

        for (TaskItem.OriginModule module : drafts.keySet()) {
            String draftText = drafts.get(module);
            TaskItem draftTask;

            if (module == TaskItem.OriginModule.DAILY) {
                draftTask = new TaskItem(draftText, TaskItem.Priority.MED, module);
                draftTask.setPrefix("[DRAFT]");
            } else {
                draftTask = new TaskItem("[DRAFT] " + draftText, TaskItem.Priority.MED, module);
            }

            taskDatabase.add(0, draftTask);
        }
        drafts.clear();
    }

    private void saveDraftsOnClose() {
        if (quickToDoPanel != null) {
            appStats.saveDraft(TaskItem.OriginModule.QUICK, quickToDoPanel.getPendingInput());
        }
        if (workListPanel != null) {
            appStats.saveDraft(TaskItem.OriginModule.WORK, workListPanel.getPendingInput());
        }
        if (dailyToDoPanel != null) {
            appStats.saveDraft(TaskItem.OriginModule.DAILY, dailyToDoPanel.getPendingInput());
        }
    }

    private void autoArchiveTasks() {
        for (TaskItem task : taskDatabase) {
            if (task.getOriginModule() == TaskItem.OriginModule.QUICK && task.isFinished() && !task.isArchived()) {
                task.setArchived(true);
                if (task.getDateCompleted() == null) {
                    task.setFinished(true);
                }
            }
        }
    }

    private void processDailyRollover(List<TaskItem> tasks, AppStats stats) {
        LocalDate today = LocalDate.now();
        LocalDate lastOpened = stats.getLastOpenedDate();

        if (today.isAfter(lastOpened)) {
            int totalDaily = 0;
            int completedDaily = 0;

            for (TaskItem task : tasks) {
                if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                    totalDaily++;
                    if (task.isFinished()) completedDaily++;
                }
            }

            if (totalDaily > 0) {
                double percentComplete = (double) completedDaily / totalDaily;
                stats.addHistoryRecord(lastOpened, percentComplete);

                if (percentComplete >= 1.0) {
                    stats.setCurrentStreak(stats.getCurrentStreak() + 1);
                } else {
                    stats.setCurrentStreak(0);
                }
            }

            for (TaskItem task : tasks) {
                if (task.getOriginModule() == TaskItem.OriginModule.DAILY && !task.isArchived()) {
                    task.setFinished(false);
                }
            }

            stats.setLastOpenedDate(today);
            StorageManager.saveStats(stats);
            StorageManager.saveTasks(tasks);
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(title + " Module (Under Construction)");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // ADD THIS LINE TO FIX ALL DROPDOWN/MENU GLITCHING:
            JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new TaskTrackerApp().setVisible(true);
        });
    }
}