package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.*;
import com.raeden.ors_to_do.modules.dependencies.ui.analytics.AgeCountdownCard;
import com.raeden.ors_to_do.modules.dependencies.ui.analytics.AnalyticsHeroCard;
import com.raeden.ors_to_do.modules.dependencies.ui.analytics.AnalyticsRPGSheet;
import com.raeden.ors_to_do.modules.dependencies.ui.analytics.AnalyticsSectionBreakdown;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

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

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("Global Analytics Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F2C94C;");
        headerBox.getChildren().add(titleLabel);
        setTop(headerBox);

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

        // 1. Crunch the Data
        int lifetimeCompletedTasks = 0; int lifetimeFocusSeconds = 0;
        int highPriorityCompleted = 0; int penaltiesSuffered = 0; int subTasksCrushed = 0;
        int[] dayCounts = new int[7];

        int challengesCompleted = 0;
        int notesCreated = 0;
        int notesArchived = 0;
        int perksGained = 0;
        int perksLost = 0;
        String mostActivePerk = "None";
        int highestPerkLevel = -1;
        int rewardsClaimed = 0;

        CustomPriority highestPrio = appStats.getCustomPriorities().isEmpty() ? null : appStats.getCustomPriorities().get(appStats.getCustomPriorities().size() - 1);
        Map<String, Integer> sectionTasksMap = new HashMap<>();
        Map<String, Integer> sectionTimeMap = new HashMap<>();

        for (TaskItem task : globalDatabase) {
            SectionConfig taskConfig = null;
            if (task.getSectionId() != null) {
                taskConfig = appStats.getSections().stream()
                        .filter(c -> c.getId().equals(task.getSectionId()))
                        .findFirst().orElse(null);
            }

            if (taskConfig != null) {
                if (taskConfig.isNotesPage()) {
                    notesCreated++;
                    if (task.isArchived()) notesArchived++;
                }
                if (taskConfig.isPerkPage()) {
                    if (task.getPerkUnlockedDate() != null || task.getPerkLevel() > 0) perksGained++;
                    if (task.getPerkLostDate() != null) perksLost++;
                    if (task.getPerkLevel() > highestPerkLevel) {
                        highestPerkLevel = task.getPerkLevel();
                        mostActivePerk = task.getTextContent();
                    }
                }
                if (taskConfig.isChallengePage() && task.isFinished()) challengesCompleted++;
                if (taskConfig.isRewardsPage() && task.isFinished()) rewardsClaimed++;
            }

            if (task.isPenaltyApplied()) penaltiesSuffered++;
            for (SubTask sub : task.getSubTasks()) if (sub.isFinished()) subTasksCrushed++;

            if (task.isFinished()) {
                if (appStats.getAnalyticsResetTimestamp() != null && task.getDateCompleted() != null && task.getDateCompleted().isBefore(appStats.getAnalyticsResetTimestamp())) continue;

                lifetimeCompletedTasks++;
                String secId = task.getSectionId() != null ? task.getSectionId() : "Unknown";
                sectionTasksMap.put(secId, sectionTasksMap.getOrDefault(secId, 0) + 1);

                if (highestPrio != null && highestPrio.equals(task.getPriority())) highPriorityCompleted++;
                if (task.getDateCompleted() != null) dayCounts[task.getDateCompleted().getDayOfWeek().getValue() - 1]++;
            }

            if (task.getTimeSpentSeconds() > 0) {
                lifetimeFocusSeconds += task.getTimeSpentSeconds();
                String secId = task.getSectionId() != null ? task.getSectionId() : "Unknown";
                sectionTimeMap.put(secId, sectionTimeMap.getOrDefault(secId, 0) + task.getTimeSpentSeconds());
            }
        }

        // 2. Format Derived Stats
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int maxDayIdx = 0, maxDayCount = 0;
        for (int i = 0; i < 7; i++) {
            if (dayCounts[i] > maxDayCount) { maxDayCount = dayCounts[i]; maxDayIdx = i; }
        }
        String mostProductiveDay = maxDayCount > 0 ? dayNames[maxDayIdx] : "N/A";

        String avgTimeStr = "0m";
        if (lifetimeCompletedTasks > 0 && lifetimeFocusSeconds > 0) {
            int avgSecs = lifetimeFocusSeconds / lifetimeCompletedTasks;
            avgTimeStr = (avgSecs / 3600 > 0 ? (avgSecs / 3600) + "h " : "") + ((avgSecs % 3600) / 60) + "m";
        }
        long hours = lifetimeFocusSeconds / 3600, mins = (lifetimeFocusSeconds % 3600) / 60;

        String highestStreakSub = appStats.getHighestStreakSection() != null && !appStats.getHighestStreakSection().equals("None")
                ? "in " + appStats.getHighestStreakSection() : "";

        // 3. Render Widgets

        // --- FIXED: Render Age Countdown completely outside the FlowPane on its own row ---
        AgeCountdownCard ageCard = new AgeCountdownCard(appStats, this::refreshData);
        dashboardContent.getChildren().add(ageCard);

        FlowPane heroFlow = new FlowPane(15, 15); // Tighter spacing for the smaller cards
        heroFlow.setAlignment(Pos.CENTER_LEFT);

        heroFlow.getChildren().addAll(
                new AnalyticsHeroCard("🏆 Global Score", String.valueOf(appStats.getGlobalScore()), "#FFD700"),
                new AnalyticsHeroCard("🔥 Highest Streak", String.valueOf(appStats.getHighestStreak()), highestStreakSub, "#FF8C00"),
                new AnalyticsHeroCard("✅ Lifetime Tasks", String.valueOf(lifetimeCompletedTasks), "#4EC9B0"),
                new AnalyticsHeroCard("⏱ Lifetime Focus", String.format("%dh %dm", hours, mins), "#569CD6"),
                new AnalyticsHeroCard("⚔️ Challenges Done", String.valueOf(challengesCompleted), "#FF8C00"),
                new AnalyticsHeroCard("✨ Perks Gained", String.valueOf(perksGained), "Lost: " + perksLost + " | Top: " + mostActivePerk, "#FFD700"),
                new AnalyticsHeroCard("🛍️ Rewards Claimed", String.valueOf(rewardsClaimed), "#569CD6"),
                new AnalyticsHeroCard("🧘 Full Focus Sessions", String.valueOf(appStats.getLifetimeFullFocusSessions()), "#C586C0"),
                new AnalyticsHeroCard("📝 Notes Created", String.valueOf(notesCreated), "Archived: " + notesArchived + " | Deleted: " + appStats.getLifetimeDeletedNotes(), "#4EC9B0"),
                new AnalyticsHeroCard("🗑 Tasks Deleted", String.valueOf(appStats.getLifetimeDeletedTasks()), "#FF6666"),
                new AnalyticsHeroCard("🎯 High Prio Crushed", String.valueOf(highPriorityCompleted), "#C586C0"),
                new AnalyticsHeroCard("💀 Missed Deadlines", String.valueOf(penaltiesSuffered), "#FF4444"),
                new AnalyticsHeroCard("🧩 Sub-Tasks Done", String.valueOf(subTasksCrushed), "#9CDCFE"),
                new AnalyticsHeroCard("📅 Best Day", mostProductiveDay, "#B5CEA8"),
                new AnalyticsHeroCard("⚡ Avg Task Time", avgTimeStr, "#CE9178")
        );

        dashboardContent.getChildren().add(heroFlow);
        if (appStats.isGlobalStatsEnabled()) dashboardContent.getChildren().add(new AnalyticsRPGSheet(appStats));
        dashboardContent.getChildren().add(new AnalyticsSectionBreakdown(appStats, sectionTasksMap, sectionTimeMap));
    }
}