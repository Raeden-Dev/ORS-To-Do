package com.raeden.ors_to_do.modules.dependencies;

import com.raeden.ors_to_do.TaskTrackerApp;
import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

import static com.raeden.ors_to_do.modules.dependencies.TaskDialogs.*;
import static com.raeden.ors_to_do.modules.dependencies.SystemTrayManager.pushNotification;

public class TaskCard extends VBox {

    private final AppStats.SectionConfig config;
    private final AppStats appStats;
    private final List<TaskItem> globalDatabase;
    private final Runnable onUpdate;
    private final TaskItem task;
    private final List<Timeline> activeTimelines;

    private final String[] DARK_PASTELS = {
            "#2C3E50", "#34495E", "#1A252C", "#2D3748", "#2A4365",
            "#2C5282", "#2B6CB0", "#234E52", "#285E61", "#2C7A7B",
            "#22543D", "#276749", "#2F855A", "#744210", "#975A16",
            "#702459", "#97266D", "#44337A", "#553C9A", "#1A202C"
    };

    public TaskCard(TaskItem task, AppStats.SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, BiConsumer<String, String> onReorder) {
        this.task = task;
        this.config = config;
        this.appStats = appStats;
        this.globalDatabase = globalDatabase;
        this.onUpdate = onUpdate;
        this.activeTimelines = activeTimelines;

        getStyleClass().add("task-row");

        String bgStyle = task.getColorHex() != null ? "-fx-background-color: " + task.getColorHex() + "; " : "";
        String borderStyle = "";

        if (config.isAllowFavorite() && task.isFavorite()) {
            borderStyle = "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 4; ";
        } else if (appStats.isMatchPriorityOutline() && config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
            borderStyle = "-fx-border-color: " + task.getPriority().getColorHex() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        }

        String originalStyle = bgStyle + borderStyle;
        setStyle(originalStyle);

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
            if (event.getGestureSource() != this && event.getDragboard().hasString()) setStyle(originalStyle + " -fx-border-color: #569CD6; -fx-border-width: 2;");
        });
        setOnDragExited(event -> setStyle(originalStyle));
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

        Rectangle sideRect = new Rectangle(5, 25);
        sideRect.setArcWidth(3); sideRect.setArcHeight(3);
        if (config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
            sideRect.setFill(Color.web(task.getPriority().getColorHex()));
        } else if (config.isShowPrefix() && appStats.isMatchDailyRectColor() && task.getPrefixColor() != null) {
            sideRect.setFill(Color.web(task.getPrefixColor()));
        } else {
            sideRect.setFill(Color.WHITE);
        }

        // Create an empty box first
        HBox metaBox = new HBox(7);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        // 1. Add the Expand/Collapse arrow first
        metaBox.getChildren().add(expandBtn);

        // 2. Add the Custom Icon BEFORE the rectangle
        if (config.isEnableIcons() && task.getIconSymbol() != null && !task.getIconSymbol().equals("None")) {
            Label customIcon = new Label(task.getIconSymbol());
            String iconColor = task.getIconColor() != null ? task.getIconColor() : "#FFFFFF";
            customIcon.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: " + (appStats.getTaskFontSize() + 2) + "px;");
            metaBox.getChildren().add(customIcon);
        }

        // 3. Add the side Rectangle last
        metaBox.getChildren().add(sideRect);

        if (config.isAllowFavorite() && task.isFavorite()) {
            Label starLabel = new Label("[⭐]");
            starLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: " + appStats.getTaskFontSize() + "px; -fx-font-weight: bold;");
            metaBox.getChildren().add(starLabel);
        }

        if (config.isShowDate()) {
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

        if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) {
            Label workTypeLabel = new Label("[" + task.getWorkType() + "]");
            workTypeLabel.getStyleClass().add("task-metadata");
            metaBox.getChildren().add(workTypeLabel);
        }

        Label textLabel = new Label(task.getTextContent());
        textLabel.setWrapText(true);
        String fontStyle = "-fx-font-size: " + appStats.getTaskFontSize() + "px; ";
        if (task.isFinished()) textLabel.setStyle(fontStyle + "-fx-strikethrough: true; -fx-text-fill: #E0E0E0;");
        else textLabel.setStyle(fontStyle + "-fx-strikethrough: false; -fx-text-fill: #E0E0E0;");

        HBox textContainer = new HBox(textLabel);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        if (config.isEnableScore() && (task.getRewardPoints() > 0 || task.getPenaltyPoints() > 0)) {
            String badgeStr = "";
            if (task.getRewardPoints() > 0) badgeStr += "🏆 +" + task.getRewardPoints() + "  ";
            if (task.getPenaltyPoints() > 0) badgeStr += "💀 -" + task.getPenaltyPoints();

            Label ptsLabel = new Label(badgeStr.trim());
            ptsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 12px;");
            ptsLabel.setPadding(new Insets(0, 10, 0, 0));
            mainRow.getChildren().add(ptsLabel);
        }

        Label deadlineLabel = null;
        if (task.getDeadline() != null) {
            deadlineLabel = new Label();
            deadlineLabel.setPadding(new Insets(0, 10, 0, 0));
            deadlineLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold; -fx-font-size: 13px;");
            Label finalDeadlineLabel = deadlineLabel;

            Runnable updateLabel = () -> {
                java.time.Duration dur = java.time.Duration.between(LocalDateTime.now(), task.getDeadline());
                if (dur.isNegative() || dur.isZero()) {
                    finalDeadlineLabel.setText("🚨 OVERDUE");

                    if (!task.isFinished() && task.getPenaltyPoints() > 0 && !task.isPenaltyApplied()) {
                        task.setPenaltyApplied(true);
                        appStats.setGlobalScore(appStats.getGlobalScore() - task.getPenaltyPoints());
                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);
                        Platform.runLater(() -> {
                            onUpdate.run();
                            pushNotification("Deadline Missed!", "Penalty applied: -" + task.getPenaltyPoints() + " points for task: " + task.getTextContent());
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

        Label timeLabel = null;
        if (config.isTrackTime()) {
            int mins = task.getTimeSpentSeconds() / 60;
            timeLabel = new Label("⏱ " + mins + "m");
            timeLabel.setPadding(new Insets(0, 10, 0, 0));
            if (mins > 0) timeLabel.setStyle("-fx-text-fill: #E06666; -fx-font-weight: bold; -fx-font-size: 13px;");
            else timeLabel.setStyle("-fx-text-fill: #858585; -fx-font-weight: bold; -fx-font-size: 13px;");
        }

        ComboBox<TaskItem.CustomPriority> prioBox = null;
        if (config.isShowPriority()) {
            ComboBox<TaskItem.CustomPriority> localPrioBox = new ComboBox<>();
            prioBox = localPrioBox;

            localPrioBox.getItems().addAll(appStats.getCustomPriorities());
            localPrioBox.setValue(task.getPriority());
            setupPriorityBoxColors(localPrioBox);

            localPrioBox.setOnAction(e -> {
                task.setPriority(localPrioBox.getValue());
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            });
        }

        HBox actionContainer = new HBox(5);
        actionContainer.setAlignment(Pos.CENTER);

        if (task.isCounterMode()) {
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

        mainRow.getChildren().addAll(metaBox, textContainer);
        if (deadlineLabel != null) mainRow.getChildren().add(deadlineLabel);
        if (timeLabel != null) mainRow.getChildren().add(timeLabel);
        if (prioBox != null) mainRow.getChildren().add(prioBox);
        mainRow.getChildren().add(actionContainer);

        VBox subTaskBox = new VBox(8);
        subTaskBox.setPadding(new Insets(0, 10, 15, 60));

        if (hasLinks || hasSubTasks) {
            subTaskBox.setVisible(task.isExpanded());
            subTaskBox.setManaged(task.isExpanded());
        } else {
            subTaskBox.setVisible(false);
            subTaskBox.setManaged(false);
        }

        if (hasLinks) {
            for (TaskItem.TaskLink linkObj : task.getTaskLinks()) {
                HBox linkRow = new HBox(10);
                linkRow.setAlignment(Pos.CENTER_LEFT);

                Label linkIcon = new Label("🔗");
                linkIcon.setStyle("-fx-text-fill: #858585; -fx-font-size: " + Math.max(10, appStats.getTaskFontSize() - 2) + "px; -fx-font-weight: bold;");

                String displayName = linkObj.getName();
                if (displayName == null || displayName.trim().isEmpty() || displayName.equalsIgnoreCase("Link")) {
                    displayName = linkObj.getUrl();
                }

                Hyperlink hyper = new Hyperlink(displayName);
                hyper.setStyle("-fx-text-fill: #569CD6; -fx-font-size: " + Math.max(10, appStats.getTaskFontSize() - 2) + "px;");
                hyper.setOnAction(e -> {
                    try { java.awt.Desktop.getDesktop().browse(new java.net.URI(linkObj.getUrl())); }
                    catch (Exception ex) { ex.printStackTrace(); }
                });

                HBox hyperContainer = new HBox(hyper);
                hyperContainer.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(hyperContainer, Priority.ALWAYS);

                Button editLinkBtn = new Button("✏️");
                editLinkBtn.setMinWidth(Region.USE_PREF_SIZE);
                editLinkBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-padding: 0;");
                editLinkBtn.setOnAction(e -> TaskDialogs.showLinkDialog(task, linkObj, globalDatabase, onUpdate));

                Button delLinkBtn = new Button("❌");
                delLinkBtn.setMinWidth(Region.USE_PREF_SIZE);
                delLinkBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand; -fx-padding: 0;");
                delLinkBtn.setOnAction(e -> {
                    task.getTaskLinks().remove(linkObj);
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                });

                linkRow.getChildren().addAll(linkIcon, hyperContainer, editLinkBtn, delLinkBtn);
                subTaskBox.getChildren().add(linkRow);
            }
        }

        if (hasSubTasks) {
            for (TaskItem.SubTask sub : task.getSubTasks()) {
                HBox subRow = new HBox(10);
                subRow.setAlignment(Pos.CENTER_LEFT);

                CheckBox subCheck = new CheckBox();
                subCheck.setSelected(sub.isFinished());
                subCheck.setOnAction(e -> { sub.setFinished(subCheck.isSelected()); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });

                Label subText = new Label("- " + sub.getTextContent());
                subText.setWrapText(true);
                int subSize = Math.max(10, appStats.getTaskFontSize() - 2);
                String strike = sub.isFinished() ? "-fx-strikethrough: true; " : "";
                subText.setStyle("-fx-font-size: " + subSize + "px; " + strike + "-fx-text-fill: #858585;");

                HBox subTextContainer = new HBox(subText);
                subTextContainer.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(subTextContainer, Priority.ALWAYS);

                Button delSubBtn = new Button("❌");
                delSubBtn.setMinWidth(Region.USE_PREF_SIZE);
                delSubBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand; -fx-padding: 0;");
                delSubBtn.setOnAction(e -> { task.getSubTasks().remove(sub); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });

                subRow.getChildren().addAll(subCheck, subTextContainer, delSubBtn);
                subTaskBox.getChildren().add(subRow);
            }
        }

        getChildren().addAll(mainRow, subTaskBox);
        attachContextMenu();
    }

    private void handleTaskCompletion(CheckBox optCheckBox) {
        if (task.getRewardPoints() > 0 && !task.isPointsClaimed()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Claim " + task.getRewardPoints() + " points? This will permanently lock the task.", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Complete Task & Claim Points");
            TaskDialogs.styleDialog(alert);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    task.setFinished(true);
                    task.setPointsClaimed(true);
                    if (task.isCounterMode() && task.getMaxCount() > 0) task.setCurrentCount(task.getMaxCount());
                    for (TaskItem.SubTask sub : task.getSubTasks()) sub.setFinished(true);

                    appStats.setGlobalScore(appStats.getGlobalScore() + task.getRewardPoints());
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
            for (TaskItem.SubTask sub : task.getSubTasks()) sub.setFinished(true);
            StorageManager.saveTasks(globalDatabase);
            onUpdate.run();
        }
    }

    private void attachContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem copyItem = new MenuItem("Copy Card Information");
        copyItem.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();
            if (config.isShowDate()) sb.append("[").append(task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("] ");
            if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) sb.append("[").append(task.getWorkType()).append("] ");
            else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) sb.append(task.getPrefix()).append(" ");

            sb.append(task.getTextContent());

            if (task.getTaskLinks() != null) {
                for (TaskItem.TaskLink linkObj : task.getTaskLinks()) {
                    String dName = linkObj.getName();
                    if (dName == null || dName.trim().isEmpty() || dName.equalsIgnoreCase("Link") || dName.equals(linkObj.getUrl())) {
                        sb.append("\n    🔗 ").append(linkObj.getUrl());
                    } else {
                        sb.append("\n    🔗 ").append(dName).append(" (").append(linkObj.getUrl()).append(")");
                    }
                }
            }

            for (TaskItem.SubTask sub : task.getSubTasks()) {
                sb.append("\n    > ").append(sub.getTextContent());
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });
        contextMenu.getItems().addAll(copyItem, new SeparatorMenuItem());

        if (config.isAllowFavorite()) {
            MenuItem favItem = new MenuItem(task.isFavorite() ? "Remove Favorite" : "Mark as Favorite");
            favItem.setOnAction(e -> {
                task.setFavorite(!task.isFavorite());
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            });
            contextMenu.getItems().addAll(favItem, new SeparatorMenuItem());
        }

        MenuItem editItem = new MenuItem(appStats.getEditMenuText());
        editItem.setOnAction(e -> TaskDialogs.showEditDialog(task, config, appStats, globalDatabase, onUpdate));

        Menu colorMenu = new Menu("Set Background Color");
        for (String hex : DARK_PASTELS) {
            MenuItem colorItem = new MenuItem("");
            Rectangle colorIcon = new Rectangle(14, 14, Color.web(hex));
            colorIcon.setStroke(Color.BLACK);
            colorItem.setGraphic(colorIcon);
            colorItem.setOnAction(e -> { task.setColorHex(hex); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });
            colorMenu.getItems().add(colorItem);
        }
        MenuItem resetColor = new MenuItem("Reset Background");
        resetColor.setOnAction(e -> { task.setColorHex(null); StorageManager.saveTasks(globalDatabase); onUpdate.run(); });
        colorMenu.getItems().addAll(new SeparatorMenuItem(), resetColor);

        MenuItem deleteItem = new MenuItem(appStats.getDeleteMenuText());
        deleteItem.setStyle("-fx-text-fill: #FF6666;");
        deleteItem.setOnAction(e -> {
            globalDatabase.remove(task);
            appStats.setLifetimeDeletedTasks(appStats.getLifetimeDeletedTasks() + 1);
            StorageManager.saveTasks(globalDatabase);
            StorageManager.saveStats(appStats);
            onUpdate.run();
        });

        contextMenu.getItems().addAll(editItem);

        if (config.isEnableLinks()) {
            MenuItem addLinkItem = new MenuItem("Add Link");
            addLinkItem.setOnAction(e -> TaskDialogs.showLinkDialog(task, null, globalDatabase, onUpdate));
            contextMenu.getItems().add(addLinkItem);
        }

        if (config.isEnableSubTasks()) {
            MenuItem addSubItem = new MenuItem("Add Sub-task");
            addSubItem.setOnAction(e -> showAddSubTaskDialog(task, globalDatabase, onUpdate));
            contextMenu.getItems().add(addSubItem);
        }

        contextMenu.getItems().add(colorMenu);

        if (config.isAllowArchive() && !task.isCounterMode()) {
            MenuItem archiveItem = new MenuItem(appStats.getArchiveMenuText());
            archiveItem.setOnAction(e -> {
                if(!task.isFinished()) task.setFinished(true);
                task.setArchived(true);
                StorageManager.saveTasks(globalDatabase); onUpdate.run();
            });
            contextMenu.getItems().add(new SeparatorMenuItem());
            contextMenu.getItems().add(archiveItem);
        }

        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(deleteItem);

        setOnContextMenuRequested(e -> contextMenu.show(this, e.getScreenX(), e.getScreenY()));
    }
}