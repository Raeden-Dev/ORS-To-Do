package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class DangerZonePanel extends VBox {
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private Runnable refreshCallback;
    private final double BUTTON_WIDTH = 200.0;

    // --- FIXED: Switched from GridPane to FlowPane to dynamically fill the right side ---
    private FlowPane wipePane;

    public DangerZonePanel(AppStats appStats, List<TaskItem> globalDatabase, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.refreshCallback = refreshCallback;

        setStyle("-fx-border-color: #FF6666; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label dangerLabel = new Label("Danger Zone");
        dangerLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Set gap between buttons (horizontal and vertical)
        wipePane = new FlowPane(15, 15);

        getChildren().addAll(dangerLabel, wipePane);
        refreshDangerZone();
    }

    public void refreshDangerZone() {
        wipePane.getChildren().clear();

        // 1. Dynamic Section Wipe Buttons
        for (SectionConfig section : appStats.getSections()) {
            Button wipeBtn = createDangerButton("Wipe " + section.getName());
            wipeBtn.setOnAction(e -> wipeList(globalDatabase, section.getId(), refreshCallback));
            wipePane.getChildren().add(wipeBtn);
        }

        // 2. Wipe Archive Button
        Button wipeArchiveBtn = createDangerButton("Empty Archive");
        wipeArchiveBtn.setOnAction(e -> wipeList(globalDatabase, "ARCHIVED_FLAG", refreshCallback));
        wipePane.getChildren().add(wipeArchiveBtn);

        // 3. Wipe ALL Tasks Button (from your screenshot)
        Button wipeAllBtn = new Button("Wipe ALL Tasks");
        wipeAllBtn.setPrefWidth(BUTTON_WIDTH);
        wipeAllBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
        wipeAllBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete ALL tasks?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Wipe ALL Tasks");
            TaskDialogs.styleDialog(alert);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    globalDatabase.clear();
                    StorageManager.saveTasks(globalDatabase);
                    refreshCallback.run();
                }
            });
        });
        wipePane.getChildren().add(wipeAllBtn);

        // 4. Reset Analytics Button
        Button resetAnalyticsBtn = createDangerButton("Reset Global Analytics");
        resetAnalyticsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently reset all analytics?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Reset Analytics");
            TaskDialogs.styleDialog(alert);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    appStats.setGlobalScore(0);
                    appStats.setCurrentStreak(0);
                    appStats.setHighestStreak(0);
                    appStats.setLifetimeDeletedTasks(0);
                    appStats.getHistoryLog().clear();
                    appStats.getAdvancedHistoryLog().clear();
                    for(TaskItem t : globalDatabase) t.setTimeSpentSeconds(0);
                    StorageManager.saveStats(appStats);
                    StorageManager.saveTasks(globalDatabase);
                    refreshCallback.run();
                }
            });
        });
        wipePane.getChildren().add(resetAnalyticsBtn);

        Button historyBtn = new Button("View Deleted Tasks History");
        historyBtn.setPrefWidth(BUTTON_WIDTH);
        historyBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand; -fx-border-color: #555555; -fx-border-radius: 3;");
        historyBtn.setOnAction(e -> {
            com.raeden.ors_to_do.modules.dependencies.ui.DeletedHistoryDialog.show(appStats, refreshCallback);
        });
        wipePane.getChildren().add(historyBtn);
    }

    private Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(BUTTON_WIDTH);
        btn.setStyle("-fx-background-color: #333333; -fx-text-fill: #FF6666; -fx-border-color: #FF6666; -fx-border-radius: 3; -fx-cursor: hand;");
        return btn;
    }

    private void wipeList(List<TaskItem> db, String targetSectionId, Runnable refresh) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear this list?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Wipe Section");
        TaskDialogs.styleDialog(alert);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                db.removeIf(task -> {
                    if ("ARCHIVED_FLAG".equals(targetSectionId)) return task.isArchived();
                    return targetSectionId.equals(task.getSectionId()) && !task.isArchived();
                });
                StorageManager.saveTasks(db);
                refresh.run();
            }
        });
    }
}