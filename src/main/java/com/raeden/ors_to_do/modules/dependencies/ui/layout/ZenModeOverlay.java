package com.raeden.ors_to_do.modules.dependencies.ui.layout;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.TaskCard;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class ZenModeOverlay extends VBox {

    private SectionConfig config;
    private AppStats appStats;
    private List<TaskItem> globalDatabase;
    private TaskItem currentZenTask = null;
    private Runnable onExitZenMode;
    private Runnable syncCallback;
    private List<Timeline> activeTimelines;
    private BiConsumer<String, String> reorderTasks;

    public ZenModeOverlay(SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onExitZenMode, Runnable syncCallback, List<Timeline> activeTimelines, BiConsumer<String, String> reorderTasks) {
        this.config = config;
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.onExitZenMode = onExitZenMode;
        this.syncCallback = syncCallback;
        this.activeTimelines = activeTimelines;
        this.reorderTasks = reorderTasks;

        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #1E1E1E;");
        setVisible(false);
    }

    public void refreshZenMode(boolean forceReroll) {
        getChildren().clear();

        List<TaskItem> validTasks = new ArrayList<>();
        for (TaskItem t : globalDatabase) {
            if (t.getSectionId() != null && t.getSectionId().equals(config.getId()) && !t.isArchived() && !t.isFinished()) {
                boolean isLocked = false;
                if (t.getDependsOnTaskIds() != null && !t.getDependsOnTaskIds().isEmpty()) {
                    isLocked = globalDatabase.stream().anyMatch(dep -> t.getDependsOnTaskIds().contains(dep.getId()) && !dep.isFinished());
                }
                if (!isLocked) validTasks.add(t);
            }
        }

        if (validTasks.isEmpty()) {
            Label msg = new Label("All caught up! No tasks available for Zen Mode.");
            msg.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 24px; -fx-font-weight: bold;");
            Button exitBtn = new Button("Exit Zen Mode");
            exitBtn.setStyle("-fx-background-color: #569CD6; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
            exitBtn.setOnAction(e -> onExitZenMode.run());
            getChildren().addAll(msg, exitBtn);
            return;
        }

        if (config.isShowPriority()) {
            int minWeight = validTasks.stream().mapToInt(t -> getPriorityWeight(t.getPriority())).min().orElse(999);
            validTasks.removeIf(t -> getPriorityWeight(t.getPriority()) > minWeight);
        }

        if (currentZenTask == null || currentZenTask.isFinished() || currentZenTask.isArchived() || forceReroll || !validTasks.contains(currentZenTask)) {
            currentZenTask = validTasks.get(new Random().nextInt(validTasks.size()));
        }

        Label zenHeader = new Label("☯ ZEN MODE");
        zenHeader.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #FF6666; -fx-effect: dropshadow(three-pass-box, #FF6666, 15, 0, 0, 0);");

        Label zenSub = new Label("Focus on this ONE task. Ignore everything else.");
        zenSub.setStyle("-fx-font-size: 18px; -fx-text-fill: #AAAAAA; -fx-padding: 0 0 40 0;");

        Runnable onZenUpdate = () -> {
            if (currentZenTask != null && currentZenTask.isFinished()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Zen Mode");
                alert.setHeaderText(null);
                alert.setContentText("Task crushed! Great job.");
                TaskDialogs.styleDialog(alert);
                if (getScene() != null && getScene().getWindow() != null) {
                    alert.initOwner(getScene().getWindow());
                }
                alert.show();
                currentZenTask = null;
            }
            refreshZenMode(false);
            if (syncCallback != null) syncCallback.run();
        };

        TaskCard zenCard = new TaskCard(currentZenTask, config, appStats, globalDatabase, onZenUpdate, activeTimelines, reorderTasks);
        zenCard.setMaxWidth(800);

        HBox cardContainer = new HBox(zenCard);
        cardContainer.setAlignment(Pos.CENTER);
        cardContainer.setPadding(new Insets(40, 0, 80, 0));

        Button rerollBtn = new Button("🎲 Reroll Task");
        rerollBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        rerollBtn.setOnAction(e -> refreshZenMode(true));

        Button exitBtn = new Button("❌ Exit Zen Mode");
        exitBtn.setStyle("-fx-background-color: #FF6666; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        exitBtn.setOnAction(e -> onExitZenMode.run());

        HBox btnBox = new HBox(20, rerollBtn, exitBtn);
        btnBox.setAlignment(Pos.CENTER);

        getChildren().addAll(zenHeader, zenSub, cardContainer, btnBox);
    }

    private int getPriorityWeight(CustomPriority p) {
        if (p == null) return -1;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }
}