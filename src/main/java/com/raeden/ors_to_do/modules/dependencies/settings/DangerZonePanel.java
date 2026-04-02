package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.util.List;

public class DangerZonePanel extends VBox {
    private GridPane wipeGrid;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private final double BUTTON_WIDTH = 200.0;

    public DangerZonePanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;

        setStyle("-fx-border-color: #FF6666; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label dangerLabel = new Label("Danger Zone");
        dangerLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 16px; -fx-font-weight: bold;");

        wipeGrid = new GridPane();
        wipeGrid.setHgap(15); wipeGrid.setVgap(15);
        refreshDangerZone();

        getChildren().addAll(dangerLabel, wipeGrid);
    }

    public void refreshDangerZone() {
        wipeGrid.getChildren().clear();
        int col = 0; int row = 0;

        for (AppStats.SectionConfig section : appStats.getSections()) {
            Button wipeBtn = createDangerButton("Wipe " + section.getName());
            wipeBtn.setOnAction(e -> wipeList(globalDatabase, section.getId(), refreshCallback));
            wipeGrid.add(wipeBtn, col, row);
            col++;
            if (col > 1) { col = 0; row++; }
        }

        Button wipeAllBtn = new Button("Wipe ALL Tasks");
        wipeAllBtn.setPrefWidth(BUTTON_WIDTH);
        wipeAllBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        wipeAllBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete ALL tasks?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) { globalDatabase.clear(); StorageManager.saveTasks(globalDatabase); refreshCallback.run(); }
            });
        });
        wipeGrid.add(wipeAllBtn, col, row);
        col++; if (col > 1) { col = 0; row++; }

        Button resetStreakBtn = createDangerButton("Reset Daily Streak");
        resetStreakBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset your daily streak to 0?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) { appStats.setCurrentStreak(0); StorageManager.saveStats(appStats); refreshCallback.run(); }
            });
        });
        wipeGrid.add(resetStreakBtn, col, row);
        Button resetAnalyticsBtn = createDangerButton("Reset Global Analytics");
        resetAnalyticsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Reset all global analytics (Score, Streaks, Lifetime Focus, Lifetime Tasks, Section Progress)?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    appStats.setGlobalScore(0);
                    appStats.setHighestStreak(0);
                    appStats.setCurrentStreak(0);
                    appStats.setLifetimeDeletedTasks(0);
                    appStats.getHistoryLog().clear();
                    appStats.getAdvancedHistoryLog().clear();
                    appStats.setAnalyticsResetTimestamp(LocalDateTime.now());
                    for(TaskItem t : globalDatabase) {
                        t.setTimeSpentSeconds(0);
                    }

                    StorageManager.saveStats(appStats);
                    StorageManager.saveTasks(globalDatabase);
                    refreshCallback.run();
                }
            });
        });
        wipeGrid.add(resetAnalyticsBtn, 0, row);
    }

    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(BUTTON_WIDTH);
        btn.setStyle("-fx-background-color: #333333; -fx-text-fill: #FF6666; -fx-border-color: #FF6666; -fx-border-radius: 3; -fx-cursor: hand;");
        return btn;
    }

    private void wipeList(List<TaskItem> db, String targetSectionId, Runnable refresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear this list?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                db.removeIf(task -> targetSectionId.equals(task.getSectionId()));
                StorageManager.saveTasks(db);
                refresh.run();
            }
        });
    }
}