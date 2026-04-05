package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsModule extends BorderPane {
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private VBox dashboardContent;

    public AnalyticsModule(AppStats appStats, List<TaskItem> globalDatabase) {
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        setPadding(new Insets(20));

        // Top Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Global Analytics Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F2C94C;");
        headerBox.getChildren().add(titleLabel);

        setTop(headerBox);

        // Scrollable Content Area
        dashboardContent = new VBox(20);
        ScrollPane scrollPane = new ScrollPane(dashboardContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        setCenter(scrollPane);

        refreshData();
    }

    public void refreshData() {
        dashboardContent.getChildren().clear();

        int lifetimeCompletedTasks = 0;
        int lifetimeFocusSeconds = 0;

        int highPriorityCompleted = 0;
        int penaltiesSuffered = 0;
        int subTasksCrushed = 0;
        int[] dayCounts = new int[7];

        CustomPriority highestPrio = null;
        if (!appStats.getCustomPriorities().isEmpty()) {
            highestPrio = appStats.getCustomPriorities().get(appStats.getCustomPriorities().size() - 1);
        }

        Map<String, Integer> sectionTasksMap = new HashMap<>();
        Map<String, Integer> sectionTimeMap = new HashMap<>();

        for (TaskItem task : globalDatabase) {

            if (task.isPenaltyApplied()) penaltiesSuffered++;

            for (SubTask sub : task.getSubTasks()) {
                if (sub.isFinished()) subTasksCrushed++;
            }

            if (task.isFinished()) {
                if (appStats.getAnalyticsResetTimestamp() != null && task.getDateCompleted() != null) {
                    if (task.getDateCompleted().isBefore(appStats.getAnalyticsResetTimestamp())) {
                        continue;
                    }
                }

                lifetimeCompletedTasks++;
                String secId = task.getSectionId() != null ? task.getSectionId() : "Unknown";
                sectionTasksMap.put(secId, sectionTasksMap.getOrDefault(secId, 0) + 1);

                if (highestPrio != null && highestPrio.equals(task.getPriority())) {
                    highPriorityCompleted++;
                }

                if (task.getDateCompleted() != null) {
                    int dayIdx = task.getDateCompleted().getDayOfWeek().getValue() - 1;
                    dayCounts[dayIdx]++;
                }
            }

            if (task.getTimeSpentSeconds() > 0) {
                lifetimeFocusSeconds += task.getTimeSpentSeconds();
                String secId = task.getSectionId() != null ? task.getSectionId() : "Unknown";
                sectionTimeMap.put(secId, sectionTimeMap.getOrDefault(secId, 0) + task.getTimeSpentSeconds());
            }
        }

        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int maxDayIdx = 0;
        int maxDayCount = 0;
        for (int i = 0; i < 7; i++) {
            if (dayCounts[i] > maxDayCount) {
                maxDayCount = dayCounts[i];
                maxDayIdx = i;
            }
        }
        String mostProductiveDay = maxDayCount > 0 ? dayNames[maxDayIdx] : "N/A";

        String avgTimeStr = "0m";
        if (lifetimeCompletedTasks > 0 && lifetimeFocusSeconds > 0) {
            int avgSecs = lifetimeFocusSeconds / lifetimeCompletedTasks;
            long avgH = avgSecs / 3600;
            long avgM = (avgSecs % 3600) / 60;
            if (avgH > 0) avgTimeStr = avgH + "h " + avgM + "m";
            else avgTimeStr = avgM + "m";
        }

        FlowPane heroFlow = new FlowPane(20, 20);
        heroFlow.setAlignment(Pos.CENTER_LEFT);

        long hours = lifetimeFocusSeconds / 3600;
        long mins = (lifetimeFocusSeconds % 3600) / 60;

        heroFlow.getChildren().addAll(
                createHeroCard("🏆 Global Score", String.valueOf(appStats.getGlobalScore()), "#FFD700"),
                createHeroCard("🔥 Highest Streak", String.valueOf(appStats.getHighestStreak()), "#FF8C00"),
                createHeroCard("✅ Lifetime Tasks", String.valueOf(lifetimeCompletedTasks), "#4EC9B0"),
                createHeroCard("⏱ Lifetime Focus", String.format("%dh %dm", hours, mins), "#569CD6"),
                createHeroCard("🗑 Tasks Deleted", String.valueOf(appStats.getLifetimeDeletedTasks()), "#FF6666"),
                createHeroCard("🎯 High Prio Crushed", String.valueOf(highPriorityCompleted), "#C586C0"),
                createHeroCard("💀 Missed Deadlines", String.valueOf(penaltiesSuffered), "#FF4444"),
                createHeroCard("🧩 Sub-Tasks Done", String.valueOf(subTasksCrushed), "#9CDCFE"),
                createHeroCard("📅 Best Day", mostProductiveDay, "#B5CEA8"),
                createHeroCard("⚡ Avg Task Time", avgTimeStr, "#CE9178")
        );

        dashboardContent.getChildren().add(heroFlow);

        // --- NEW: Render Custom RPG Stats ---
        if (appStats.isGlobalStatsEnabled() && !appStats.getCustomStats().isEmpty()) {
            VBox rpgBox = new VBox(15);
            rpgBox.setPadding(new Insets(20, 0, 0, 0));

            Label rpgHeader = new Label("Current RPG Stats");
            rpgHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #B5CEA8;");

            FlowPane statsFlow = new FlowPane(15, 15);

            for (CustomStat stat : appStats.getCustomStats()) {
                int currentXp = appStats.getStatXpMap().getOrDefault(stat.getId(), 0);

                VBox badge = new VBox(5);
                badge.setAlignment(Pos.CENTER);
                String bgC = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";
                badge.setStyle("-fx-background-color: " + bgC + "; -fx-padding: 15 25; -fx-background-radius: 8; -fx-border-color: #3E3E42; -fx-border-radius: 8;");

                String icon = stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "";
                Label nameLbl = new Label(icon + stat.getName());
                String txtC = stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF";
                nameLbl.setStyle("-fx-text-fill: " + txtC + "; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label xpLbl = new Label(currentXp + "");
                xpLbl.setStyle("-fx-text-fill: " + txtC + "; -fx-font-size: 18px; -fx-font-weight: bold;");

                badge.getChildren().addAll(nameLbl, xpLbl);
                statsFlow.getChildren().add(badge);
            }

            rpgBox.getChildren().addAll(rpgHeader, statsFlow);
            dashboardContent.getChildren().add(rpgBox);
        }

        // Build Section-by-Section Breakdown
        Label breakdownLabel = new Label("Section Breakdown");
        breakdownLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E0E0E0;");
        breakdownLabel.setPadding(new Insets(20, 0, 5, 0));
        dashboardContent.getChildren().add(breakdownLabel);

        VBox breakdownList = new VBox(10);

        for (SectionConfig config : appStats.getSections()) {
            int tasks = sectionTasksMap.getOrDefault(config.getId(), 0);
            int timeSecs = sectionTimeMap.getOrDefault(config.getId(), 0);

            if (tasks > 0 || timeSecs > 0) {
                breakdownList.getChildren().add(createSectionRow(config.getName(), config.getSidebarColor(), tasks, timeSecs));
            }
        }

        int legacyTasks = sectionTasksMap.getOrDefault("Unknown", 0);
        int legacyTime = sectionTimeMap.getOrDefault("Unknown", 0);
        if (legacyTasks > 0 || legacyTime > 0) {
            breakdownList.getChildren().add(createSectionRow("Legacy/Deleted Sections", "#858585", legacyTasks, legacyTime));
        }

        if (breakdownList.getChildren().isEmpty()) {
            Label empty = new Label("No data available yet. Start crushing tasks!");
            empty.setStyle("-fx-text-fill: #858585; -fx-font-style: italic;");
            breakdownList.getChildren().add(empty);
        }

        dashboardContent.getChildren().add(breakdownList);
    }

    private VBox createHeroCard(String title, String value, String colorHex) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(200, 120);
        card.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 32px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, valLabel);
        return card;
    }

    private HBox createSectionRow(String sectionName, String colorHex, int tasksCompleted, int timeSecs) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-background-radius: 5;");

        javafx.scene.shape.Rectangle colorRect = new javafx.scene.shape.Rectangle(12, 12);
        colorRect.setArcWidth(3); colorRect.setArcHeight(3);
        colorRect.setFill(Color.web(colorHex));

        Label nameLabel = new Label(sectionName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setPrefWidth(200);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label tasksLabel = new Label(tasksCompleted + " Tasks Done");
        tasksLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 14px; -fx-font-weight: bold;");
        tasksLabel.setPrefWidth(120);

        long h = timeSecs / 3600;
        long m = (timeSecs % 3600) / 60;
        Label timeLabel = new Label(String.format("⏱ %dh %dm", h, m));
        timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-size: 14px; -fx-font-weight: bold;");
        timeLabel.setPrefWidth(100);

        row.getChildren().addAll(colorRect, nameLabel, spacer, tasksLabel, timeLabel);
        return row;
    }
}