package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs.*;
import static com.raeden.ors_to_do.modules.dependencies.services.SystemTrayManager.pushNotification;

public class TaskCard extends VBox {

    private final SectionConfig config;
    private final AppStats appStats;
    private final List<TaskItem> globalDatabase;
    private final Runnable onUpdate;
    private final TaskItem task;
    private final List<Timeline> activeTimelines;
    private final Consumer<TaskItem> onGoToPage;

    public TaskCard(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, BiConsumer<String, String> onReorder) {
        this(task, config, appStats, globalDatabase, onUpdate, activeTimelines, onReorder, null);
    }

    public TaskCard(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, BiConsumer<String, String> onReorder, Consumer<TaskItem> onGoToPage) {
        this.task = task;
        this.config = config;
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.onUpdate = onUpdate;
        this.activeTimelines = activeTimelines;
        this.onGoToPage = onGoToPage;

        VBox primaryCard = new VBox();
        primaryCard.getStyleClass().add("task-row");

        List<String> blockingTaskNames = new ArrayList<>();
        if (task.getDependsOnTaskIds() != null && !task.getDependsOnTaskIds().isEmpty()) {
            for (TaskItem t : globalDatabase) {
                if (task.getDependsOnTaskIds().contains(t.getId()) && !t.isFinished()) {
                    blockingTaskNames.add(t.getTextContent());
                }
            }
        }
        boolean isLocked = !blockingTaskNames.isEmpty();
        boolean isNoteMode = config.isNotesPage();

        String bgStyle = task.getColorHex() != null && !task.getColorHex().equals("transparent") ? "-fx-background-color: " + task.getColorHex() + "; " : "";
        String borderStyle = "";

        if (isNoteMode && task.getCustomOutlineColor() != null && !task.getCustomOutlineColor().equals("transparent")) {
            borderStyle = "-fx-border-color: " + task.getCustomOutlineColor() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (!isLocked) {
            if (config.isAllowFavorite() && task.isFavorite()) {
                borderStyle = "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 4; ";
            } else if (appStats.isMatchPriorityOutline() && config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
                borderStyle = "-fx-border-color: " + task.getPriority().getColorHex() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
            }
        }

        String originalStyle = bgStyle + borderStyle;
        primaryCard.setStyle(originalStyle);

        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            db.setContent(content);
            event.consume();
        });
        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        setOnDragEntered(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString())
                primaryCard.setStyle(originalStyle + " -fx-border-color: #569CD6; -fx-border-width: 2;");
        });
        setOnDragExited(event -> primaryCard.setStyle(originalStyle));
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                onReorder.accept(db.getString(), task.getId());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));

        boolean hasLinks = config.isEnableLinks() && task.getTaskLinks() != null && !task.getTaskLinks().isEmpty();
        boolean hasSubTasks = config.isEnableSubTasks() && !task.getSubTasks().isEmpty();

        Button expandBtn = new Button(task.isExpanded() ? "▼" : "▶");
        expandBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand;");
        if (!hasSubTasks && !hasLinks) { expandBtn.setVisible(false); expandBtn.setManaged(false); }
        expandBtn.setOnAction(e -> { task.setExpanded(!task.isExpanded()); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });

        HBox metaBox = new HBox(7);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.getChildren().add(expandBtn);

        HBox textContainer = new HBox(5);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        if (!isLocked) {
            Region sideRect = new Region();
            sideRect.setMinWidth(5);
            sideRect.setPrefWidth(5);

            if (isNoteMode) {
                sideRect.minHeightProperty().bind(textContainer.heightProperty());
            } else {
                sideRect.setPrefHeight(25);
                sideRect.setMaxHeight(25);
            }

            String fillColor = "transparent";
            if (isNoteMode && task.getCustomSideboxColor() != null && !task.getCustomSideboxColor().equals("transparent")) {
                fillColor = task.getCustomSideboxColor();
            } else if (config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
                fillColor = task.getPriority().getColorHex();
            } else if (config.isShowPrefix() && appStats.isMatchDailyRectColor() && task.getPrefixColor() != null) {
                fillColor = task.getPrefixColor();
            } else {
                fillColor = "#FFFFFF";
            }
            sideRect.setStyle("-fx-background-color: " + fillColor + "; -fx-background-radius: 3;");


            if (config.isEnableIcons() && task.getIconSymbol() != null && !task.getIconSymbol().equals("None")) {
                Label customIcon = new Label(task.getIconSymbol());
                String iconColor = task.getIconColor() != null ? task.getIconColor() : "#FFFFFF";
                customIcon.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: " + (appStats.getTaskFontSize() + 2) + "px;");
                metaBox.getChildren().add(customIcon);
            }

            metaBox.getChildren().add(sideRect);

            if (config.isAllowFavorite() && task.isFavorite() && !isNoteMode) {
                Label starLabel = new Label("[⭐]");
                starLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-font-weight: bold;");
                metaBox.getChildren().add(starLabel);
            }

            if (config.isShowDate() && !isNoteMode) {
                Label dateLabel = new Label("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
                dateLabel.getStyleClass().add("task-metadata");
                metaBox.getChildren().add(dateLabel);
            }

            if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) {
                Label prefixLabel = new Label(task.getPrefix());
                prefixLabel.getStyleClass().add("task-prefix");
                String pColor = task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0";
                prefixLabel.setStyle("-fx-text-fill: " + pColor + "; -fx-font-size: " + appStats.getTaskFontSize() + "px;");
                metaBox.getChildren().add(prefixLabel);
            }

            if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty() && !isNoteMode) {
                Label workTypeLabel = new Label("[" + task.getWorkType() + "]");
                workTypeLabel.getStyleClass().add("task-metadata");
                metaBox.getChildren().add(workTypeLabel);
            }
        }

        String fontStyle = "-fx-font-size: " + appStats.getTaskFontSize() + "px; ";

        if (isLocked) {
            textContainer.setAlignment(Pos.CENTER);

            Label lockIcon = new Label("🔒");
            lockIcon.setStyle("-fx-text-fill: #FF6666; -fx-font-size: " + (appStats.getTaskFontSize() + 2) + "px;");

            String lockedText = blockingTaskNames.size() == 1 ? blockingTaskNames.get(0) : blockingTaskNames.size() + " tasks";
            Label lockMsg = new Label("Complete [" + lockedText + "] to unlock");
            lockMsg.setStyle(fontStyle + "-fx-text-fill: #858585; -fx-font-style: italic;");

            textContainer.getChildren().addAll(lockIcon, lockMsg);
        } else {
            textContainer.setAlignment(Pos.CENTER_LEFT);
            Label textLabel = new Label(task.getTextContent());
            textLabel.setWrapText(true);
            if (task.isFinished() && !isNoteMode) textLabel.setStyle(fontStyle + "-fx-strikethrough: true; -fx-text-fill: #E0E0E0;");
            else textLabel.setStyle(fontStyle + "-fx-strikethrough: false; -fx-text-fill: #E0E0E0;");
            textContainer.getChildren().add(textLabel);
        }

        mainRow.getChildren().addAll(metaBox, textContainer);

        // --- FIXED: RPG Stats Mini-Card Full Width & State Memory ---
        FlowPane statsMiniCard = new FlowPane(8, 8);
        statsMiniCard.setPadding(new Insets(8, 12, 8, 12));
        statsMiniCard.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #3E3E42; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

        // Let the box span the full width
        statsMiniCard.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(statsMiniCard, new Insets(4, 0, 0, 0)); // No left indent, exactly matching top width

        // Grab the saved state so it doesn't close on refresh
        boolean isStatsVis = task.isStatsExpanded();
        statsMiniCard.setVisible(isStatsVis);
        statsMiniCard.setManaged(isStatsVis);

        boolean hasAnyStats = false;

        if (config.isRewardsPage() && task.getCostPoints() > 0) {
            Label pLabel = new Label("💎 -" + task.getCostPoints() + " Cost");
            pLabel.setStyle("-fx-text-fill: #9CDCFE; -fx-font-size: 11px; -fx-background-color: #1A3A4D; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
            statsMiniCard.getChildren().add(pLabel);
            hasAnyStats = true;
        } else if (config.isEnableScore()) {
            if (task.getRewardPoints() > 0) {
                Label pLabel = new Label("🏆 +" + task.getRewardPoints() + " Pts");
                pLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-background-color: #332B00; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
                statsMiniCard.getChildren().add(pLabel);
                hasAnyStats = true;
            }
            if (task.getPenaltyPoints() > 0) {
                Label pLabel = new Label("💀 -" + task.getPenaltyPoints() + " Pts");
                pLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 11px; -fx-background-color: #330000; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
                statsMiniCard.getChildren().add(pLabel);
                hasAnyStats = true;
            }
        }

        if (config.isEnableStatsSystem()) {
            for (CustomStat stat : appStats.getCustomStats()) {
                int rew = task.getStatRewards().getOrDefault(stat.getId(), 0);
                if (rew > 0) {
                    String icon = stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "";
                    String bgC = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";
                    String txtC = stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF";
                    Label sLabel = new Label(icon + "+" + rew + " " + stat.getName());
                    sLabel.setStyle("-fx-text-fill: " + txtC + "; -fx-background-color: " + bgC + "; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");
                    statsMiniCard.getChildren().add(sLabel);
                    hasAnyStats = true;
                }
                int pen = task.getStatPenalties().getOrDefault(stat.getId(), 0);
                if (pen > 0) {
                    String icon = stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "";
                    String bgC = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";
                    Label sLabel = new Label(icon + "-" + pen + " " + stat.getName());
                    sLabel.setStyle("-fx-text-fill: #FF6666; -fx-background-color: " + bgC + "; -fx-font-size: 11px; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-border-color: #FF6666; -fx-border-radius: 4;");
                    statsMiniCard.getChildren().add(sLabel);
                    hasAnyStats = true;
                }
            }
        }

        // --- FIXED: Toggle saves state silently without causing refresh blur ---
        if (hasAnyStats && !isNoteMode) {
            Button eyeBtn = new Button("👁");
            eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0 10 0 0; -fx-text-fill: " + (task.isStatsExpanded() ? "#569CD6" : "#AAAAAA") + ";");
            eyeBtn.setOnAction(e -> {
                boolean newVis = !task.isStatsExpanded();
                task.setStatsExpanded(newVis);
                statsMiniCard.setVisible(newVis);
                statsMiniCard.setManaged(newVis);
                eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 0 10 0 0; -fx-text-fill: " + (newVis ? "#569CD6" : "#AAAAAA") + ";");
                StorageManager.saveTasks(globalDatabase); // Save so state survives the next refreshList()
            });
            mainRow.getChildren().add(eyeBtn);
        }

        Label deadlineLabel = null;
        if (task.getDeadline() != null && !isNoteMode) {
            deadlineLabel = new Label();
            deadlineLabel.setPadding(new Insets(0, 10, 0, 0));
            deadlineLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold; -fx-font-size: 13px;");
            Label finalDeadlineLabel = deadlineLabel;

            Runnable updateLabel = () -> {
                java.time.Duration dur = java.time.Duration.between(LocalDateTime.now(), task.getDeadline());
                if (dur.isNegative() || dur.isZero()) {
                    finalDeadlineLabel.setText("🚨 OVERDUE");

                    boolean hasAnyPenalty = task.getPenaltyPoints() > 0 || (config.isEnableStatsSystem() && !task.getStatPenalties().isEmpty());

                    if (!task.isFinished() && hasAnyPenalty && !task.isPenaltyApplied()) {
                        task.setPenaltyApplied(true);
                        appStats.setGlobalScore(appStats.getGlobalScore() - task.getPenaltyPoints());

                        if (config.isEnableStatsSystem()) {
                            task.getStatPenalties().forEach((statId, amount) -> appStats.addStatXp(statId, -amount));
                        }

                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);
                        Platform.runLater(() -> {
                            onUpdate.run();
                            pushNotification("Deadline Missed!", "Penalties applied for task: " + task.getTextContent());
                        });
                    }

                } else {
                    long totalSecs = dur.getSeconds();
                    long days = totalSecs / 86400;
                    long hours = (totalSecs % 86400) / 3600;
                    long mins = (totalSecs % 3600) / 60;
                    long secs = totalSecs % 60;
                    if (days > 0) finalDeadlineLabel.setText(String.format("⏳ %dd %02d:%02d:%02d", days, hours, mins, secs));
                    else finalDeadlineLabel.setText(String.format("⏳ %02d:%02d:%02d", hours, mins, secs));
                }
            };
            updateLabel.run();

            Timeline deadlineTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateLabel.run()));
            deadlineTimer.setCycleCount(Animation.INDEFINITE);
            deadlineTimer.play();
            activeTimelines.add(deadlineTimer);
        }

        if (!isLocked) {
            if (deadlineLabel != null) mainRow.getChildren().add(deadlineLabel);

            if (config.isTrackTime() && !isNoteMode) {
                int mins = task.getTimeSpentSeconds() / 60;
                Label timeLabel = new Label("⏱ " + mins + "m");
                timeLabel.setPadding(new Insets(0, 10, 0, 0));
                if (mins > 0) timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
                else timeLabel.setStyle("-fx-text-fill: #858585; -fx-font-weight: bold; -fx-font-size: 13px;");
                mainRow.getChildren().add(timeLabel);
            }

            if (config.isShowPriority() && !isNoteMode) {
                ComboBox<CustomPriority> localPrioBox = new ComboBox<>();
                localPrioBox.getItems().addAll(appStats.getCustomPriorities());
                localPrioBox.setValue(task.getPriority());
                setupPriorityBoxColors(localPrioBox);

                localPrioBox.setOnAction(e -> {
                    task.setPriority(localPrioBox.getValue());
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                });
                mainRow.getChildren().add(localPrioBox);
            }

            HBox actionContainer = new HBox(5);
            actionContainer.setAlignment(Pos.CENTER);

            if (isNoteMode) {
                Button pinBtn = new Button("📌");
                String pinColor = task.isPinned() ? "#FF6666" : "#FFFFFF";
                double opacity = task.isPinned() ? 1.0 : 0.5;
                pinBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: " + pinColor + "; -fx-opacity: " + opacity + ";");
                pinBtn.setOnAction(e -> {
                    task.setPinned(!task.isPinned());
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                });
                actionContainer.getChildren().add(pinBtn);
            } else if (config.isRewardsPage()) {
                Button buyBtn = new Button(task.isCounterMode() ? "Buy (" + task.getCurrentCount() + "/" + task.getMaxCount() + ")" : "Buy");
                buyBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                if (task.isFinished()) buyBtn.setDisable(true);
                buyBtn.setOnAction(e -> handleRewardPurchase());
                actionContainer.getChildren().add(buyBtn);
            } else if (task.isCounterMode()) {
                Button minusBtn = new Button("-");
                minusBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
                if (task.isPointsClaimed() || task.isFinished()) minusBtn.setDisable(true);

                Label countLabel = new Label(task.getCurrentCount() + (task.getMaxCount() > 0 ? " / " + task.getMaxCount() : ""));
                countLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 5;");

                Button plusBtn = new Button("+");
                plusBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
                if (task.isPointsClaimed() || task.isFinished()) plusBtn.setDisable(true);

                minusBtn.setOnAction(e -> {
                    if (task.getCurrentCount() > 0 && !task.isPointsClaimed()) {
                        task.setCurrentCount(task.getCurrentCount() - 1);
                        task.setFinished(false);
                        StorageManager.saveTasks(globalDatabase);
                        onUpdate.run();
                    }
                });

                plusBtn.setOnAction(e -> {
                    if (!task.isPointsClaimed()) {
                        task.setCurrentCount(task.getCurrentCount() + 1);
                        if (task.getMaxCount() > 0 && task.getCurrentCount() >= task.getMaxCount()) {
                            handleTaskCompletion(null);
                        } else {
                            StorageManager.saveTasks(globalDatabase);
                            onUpdate.run();
                        }
                    }
                });

                actionContainer.getChildren().addAll(minusBtn, countLabel, plusBtn);

            } else {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(task.isFinished());
                if (task.isPointsClaimed()) checkBox.setDisable(true);

                checkBox.setOnAction(e -> {
                    if (checkBox.isSelected()) {
                        handleTaskCompletion(checkBox);
                    } else {
                        task.setFinished(false);
                        StorageManager.saveTasks(globalDatabase);
                        onUpdate.run();
                    }
                });
                actionContainer.getChildren().add(checkBox);
            }

            boolean hasUnfinishedSubTasks = !task.isFinished() && config.isEnableSubTasks() && !task.getSubTasks().isEmpty() && task.getSubTasks().stream().anyMatch(sub -> !sub.isFinished());

            if (hasUnfinishedSubTasks && !isNoteMode) {
                for (javafx.scene.Node n : actionContainer.getChildren()) {
                    n.setDisable(true);
                }

                Label subLockIcon = new Label("🔒");
                subLockIcon.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 14px; -fx-cursor: hand;");
                subLockIcon.setPadding(new Insets(0, 5, 0, 0));

                Tooltip t = new Tooltip("Complete all sub-tasks to unlock!");
                t.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: #FF6666; -fx-border-color: #FF6666; -fx-font-size: 12px;");
                Tooltip.install(subLockIcon, t);

                actionContainer.getChildren().add(0, subLockIcon);
            }

            mainRow.getChildren().add(actionContainer);
        }

        SubTaskRenderer subTaskBox = new SubTaskRenderer(task, config, appStats, globalDatabase, onUpdate);

        primaryCard.getChildren().addAll(mainRow, subTaskBox);

        getChildren().addAll(primaryCard, statsMiniCard);

        ContextMenu contextMenu = TaskContextMenu.build(task, config, appStats, globalDatabase, onUpdate, onGoToPage);
        primaryCard.setOnContextMenuRequested(e -> contextMenu.show(primaryCard, e.getScreenX(), e.getScreenY()));
    }

    private void handleRewardPurchase() {
        if (appStats.getGlobalScore() < task.getCostPoints()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough points! You need " + task.getCostPoints() + " but only have " + appStats.getGlobalScore() + ".");
            alert.setHeaderText("Cannot Buy Reward");
            TaskDialogs.styleDialog(alert);
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Buy '" + task.getTextContent() + "' for " + task.getCostPoints() + " points?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Purchase");
        TaskDialogs.styleDialog(alert);

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                appStats.setGlobalScore(appStats.getGlobalScore() - task.getCostPoints());
                appStats.setLifetimePointsSpent(appStats.getLifetimePointsSpent() + task.getCostPoints());
                appStats.setRewardsClaimed(appStats.getRewardsClaimed() + 1);

                if (task.isCounterMode()) {
                    task.setCurrentCount(task.getCurrentCount() + 1);
                    if (task.getMaxCount() > 0 && task.getCurrentCount() >= task.getMaxCount()) {
                        task.setFinished(true);
                    }
                }

                StorageManager.saveStats(appStats);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
                pushNotification("Reward Claimed!", "You bought: " + task.getTextContent());
            }
        });
    }

    private void handleTaskCompletion(CheckBox optCheckBox) {
        boolean hasRewards = task.getRewardPoints() > 0 || (config.isEnableStatsSystem() && !task.getStatRewards().isEmpty());

        if (hasRewards && !task.isPointsClaimed()) {

            StringBuilder rewardStr = new StringBuilder();
            if (task.getRewardPoints() > 0) rewardStr.append("+").append(task.getRewardPoints()).append(" Global Points\n");

            if (config.isEnableStatsSystem()) {
                for (CustomStat stat : appStats.getCustomStats()) {
                    int amt = task.getStatRewards().getOrDefault(stat.getId(), 0);
                    if (amt > 0) rewardStr.append("+").append(amt).append(" ").append(stat.getName()).append(" XP\n");
                }
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Claim the following rewards?\n\n" + rewardStr.toString().trim() + "\n\nThis will permanently lock the task.", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Complete Task & Claim Rewards");
            TaskDialogs.styleDialog(alert);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    task.setFinished(true);
                    task.setPointsClaimed(true);
                    if (task.isCounterMode() && task.getMaxCount() > 0) task.setCurrentCount(task.getMaxCount());
                    for (SubTask sub : task.getSubTasks()) sub.setFinished(true);

                    appStats.setGlobalScore(appStats.getGlobalScore() + task.getRewardPoints());
                    if (config.isEnableStatsSystem()) {
                        task.getStatRewards().forEach((statId, amount) -> appStats.addStatXp(statId, amount));
                    }

                    StorageManager.saveStats(appStats);
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                } else {
                    if (optCheckBox != null) optCheckBox.setSelected(false);
                    if (task.isCounterMode()) task.setCurrentCount(task.getCurrentCount() - 1);
                    onUpdate.run();
                }
            });
        } else {
            task.setFinished(true);
            if (task.isCounterMode() && task.getMaxCount() > 0) task.setCurrentCount(task.getMaxCount());
            for (SubTask sub : task.getSubTasks()) sub.setFinished(true);
            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        }
    }
}