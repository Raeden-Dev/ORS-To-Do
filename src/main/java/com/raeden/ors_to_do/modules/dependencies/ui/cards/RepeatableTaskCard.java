package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.services.SystemTrayManager;
import com.raeden.ors_to_do.modules.dependencies.ui.components.TaskStatsMiniCard;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskActionHandler;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class RepeatableTaskCard extends VBox {

    public RepeatableTaskCard(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, java.util.function.BiConsumer<String, String> onReorder) {
        super();
        getStyleClass().add("task-row");

        String baseColor = "#3E3E42";
        String bgColor = "#252526";

        if (config != null && config.isEnableTaskStyling()) {
            if (task.getColorHex() != null && !task.getColorHex().equals("transparent")) bgColor = task.getColorHex();
            if (task.getCustomOutlineColor() != null && !task.getCustomOutlineColor().equals("transparent")) baseColor = task.getCustomOutlineColor();
        }
        setStyle("-fx-border-color: " + baseColor + "; -fx-border-radius: 5; -fx-background-color: " + bgColor + "; -fx-background-radius: 5;");
        setPadding(new Insets(10));

        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // --- NEW: Repeatable Clicker Button ---
        Button repeatBtn = new Button("↻");
        repeatBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4EC9B0; -fx-font-size: 18px; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 0;");

        Label repCountLabel = new Label(String.valueOf(task.getRepetitionCount()));
        repCountLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox clickerBox = new HBox(5, repCountLabel, repeatBtn);
        clickerBox.setAlignment(Pos.CENTER);

        repeatBtn.setOnAction(e -> {
            task.setRepetitionCount(task.getRepetitionCount() + 1);
            repCountLabel.setText(String.valueOf(task.getRepetitionCount()));

            // Re-apply RPG stats and score points natively as if the task was "finished"
            TaskActionHandler.processRPGStats(task, appStats, true);
            if (config != null && config.isEnableScore()) {
                appStats.setGlobalScore(appStats.getGlobalScore() + task.getRewardPoints());
                appStats.setGlobalScore(appStats.getGlobalScore() - task.getPenaltyPoints());
            }

            StorageManager.saveStats(appStats);
            StorageManager.saveTasks(globalDatabase);
            SystemTrayManager.pushNotification("Repetition Logged", "You repeated: " + task.getTextContent());
            onUpdate.run();
        });

        mainRow.getChildren().add(clickerBox);

        // --- Standard Meta Information (from TaskCard) ---
        VBox metaCol = new VBox(5);
        metaCol.setAlignment(Pos.CENTER_LEFT);

        HBox tagsRow = new HBox(8);
        tagsRow.setAlignment(Pos.CENTER_LEFT);

        if (config != null) {
            if (config.isShowPriority() && task.getPriority() != null) {
                Label prioLabel = new Label(task.getPriority().getName());
                prioLabel.setStyle("-fx-background-color: " + task.getPriority().getColorHex() + "33; -fx-text-fill: " + task.getPriority().getColorHex() + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 3; -fx-border-color: " + task.getPriority().getColorHex() + "; -fx-border-radius: 3;");
                tagsRow.getChildren().add(prioLabel);
            }
            if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) {
                Label prefixLabel = new Label(task.getPrefix());
                prefixLabel.setStyle("-fx-text-fill: " + task.getPrefixColor() + "; -fx-font-weight: bold; -fx-font-size: 11px;");
                tagsRow.getChildren().add(prefixLabel);
            }
            if (config.isShowTaskType() && task.getTaskType() != null && !task.getTaskType().isEmpty()) {
                Label typeLabel = new Label(task.getTaskType());
                typeLabel.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: #E0E0E0; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;");
                tagsRow.getChildren().add(typeLabel);
            }
        }
        if (!tagsRow.getChildren().isEmpty()) metaCol.getChildren().add(tagsRow);

        HBox textRow = new HBox(8);
        textRow.setAlignment(Pos.CENTER_LEFT);

        if (config != null && config.isEnableIcons() && task.getIconSymbol() != null && !task.getIconSymbol().equals("None")) {
            Label iconLbl = new Label(task.getIconSymbol());
            iconLbl.setStyle("-fx-text-fill: " + (task.getIconColor() != null ? task.getIconColor() : "#FFFFFF") + "; -fx-font-size: " + appStats.getTaskFontSize() + "px;");
            textRow.getChildren().add(iconLbl);
        }

        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-text-fill: #E0E0E0;");
        textRow.getChildren().add(textLabel);
        metaCol.getChildren().add(textRow);

        HBox.setHgrow(metaCol, Priority.ALWAYS);
        mainRow.getChildren().add(metaCol);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        mainRow.getChildren().add(spacer);

        Button editBtn = new Button("⚙");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand;");
        editBtn.setOnAction(e -> com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskEditDialog.showEditDialog(task, config, appStats, globalDatabase, onUpdate));
        mainRow.getChildren().add(editBtn);

        getChildren().add(mainRow);

        // Render Stats Mini Card below
        TaskStatsMiniCard statsMiniCard = new TaskStatsMiniCard(task, config, appStats, false);
        if (statsMiniCard.hasAnyStats() && task.isStatsExpanded()) {
            VBox.setMargin(statsMiniCard, new Insets(0, 0, 0, 45));
            getChildren().add(statsMiniCard);
        }

        // --- Context Menu ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Permanently Delete");
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete repeating task?", ButtonType.YES, ButtonType.NO);
            TaskDialogs.styleDialog(confirm);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    globalDatabase.remove(task);
                    appStats.setLifetimeDeletedTasks(appStats.getLifetimeDeletedTasks() + 1);
                    StorageManager.saveTasks(globalDatabase);
                    StorageManager.saveStats(appStats);
                    onUpdate.run();
                }
            });
        });
        contextMenu.getItems().add(deleteItem);
        this.setOnContextMenuRequested(e -> contextMenu.show(this, e.getScreenX(), e.getScreenY()));

        // --- Drag and Drop functionality ---
        this.setOnDragDetected(e -> {
            javafx.scene.input.Dragboard db = this.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(task.getId()); db.setContent(content);
            e.consume();
        });
        this.setOnDragOver(e -> {
            if (e.getGestureSource() != this && e.getDragboard().hasString()) e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            e.consume();
        });
        this.setOnDragDropped(e -> {
            if (e.getDragboard().hasString()) {
                onReorder.accept(e.getDragboard().getString(), task.getId());
                e.setDropCompleted(true);
            } else e.setDropCompleted(false);
            e.consume();
        });
    }
}