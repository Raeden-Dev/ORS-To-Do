package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.components.SubTaskRenderer;
import com.raeden.ors_to_do.modules.dependencies.ui.components.TaskActionControls;
import com.raeden.ors_to_do.modules.dependencies.ui.components.TaskDeadlineLabel;
import com.raeden.ors_to_do.modules.dependencies.ui.components.TaskStatsMiniCard;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.ui.menus.TaskContextMenu;
import com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskCardStyleHelper;
import com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskLinkUtil;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskCard extends VBox {

    public TaskCard(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, BiConsumer<String, String> onReorder) {
        this(task, config, appStats, globalDatabase, onUpdate, activeTimelines, onReorder, null);
    }

    public TaskCard(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate, List<Timeline> activeTimelines, BiConsumer<String, String> onReorder, Consumer<TaskItem> onGoToPage) {
        this.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(TaskDialogs.getCheckboxThemeCss(appStats.getCheckboxTheme()).getBytes()));

        int baseFontSize = appStats.getTaskFontSize();
        int metaFontSize = Math.max(10, baseFontSize - 2);
        boolean isNoteMode = config.isNotesPage();

        // 1. Calculate Lock State
        List<String> blockingTaskNames = new ArrayList<>();
        if (task.getDependsOnTaskIds() != null && !task.getDependsOnTaskIds().isEmpty()) {
            for (TaskItem t : globalDatabase) {
                if (task.getDependsOnTaskIds().contains(t.getId()) && !t.isFinished()) blockingTaskNames.add(t.getTextContent());
            }
        }
        boolean isLocked = !blockingTaskNames.isEmpty();

        // 2. Setup Base Container & Styling
        VBox primaryCard = new VBox();
        primaryCard.getStyleClass().add("task-row");
        String originalStyle = TaskCardStyleHelper.getBaseStyle(task, config, appStats, isLocked);
        primaryCard.setStyle(originalStyle);
        TaskCardStyleHelper.setupDragAndDrop(this, primaryCard, task, originalStyle, onReorder);

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10));
        if (task.isLinkCard()) mainRow.setCursor(javafx.scene.Cursor.HAND);

        mainRow.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                TaskDialogs.showEditDialog(task, config, appStats, globalDatabase, onUpdate);
            } else if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY && task.isLinkCard()) {
                TaskLinkUtil.openActionPath(task.getLinkActionPath());
            }
        });

        // 3. Build Left-Side Info (Meta Box)
        HBox metaBox = buildMetaBox(task, config, appStats, isLocked, isNoteMode, baseFontSize, metaFontSize, globalDatabase, onUpdate);

        // 4. Build Text Content
        HBox textContainer = buildTextContainer(task, isLocked, baseFontSize, metaFontSize, isNoteMode, blockingTaskNames);

        mainRow.getChildren().addAll(metaBox, textContainer);

        // 5. Expandable Stats & Deadline
        TaskStatsMiniCard statsMiniCard = new TaskStatsMiniCard(task, config, appStats, isLocked);
        if (statsMiniCard.hasAnyStats() && !isNoteMode && !isLocked) {
            Button eyeBtn = new Button("✦");
            eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: " + baseFontSize + "px; -fx-padding: 0 10 0 0; -fx-text-fill: " + (task.isStatsExpanded() ? "#569CD6" : "#AAAAAA") + ";");
            eyeBtn.setOnAction(e -> {
                task.setStatsExpanded(!task.isStatsExpanded());
                statsMiniCard.setVisible(task.isStatsExpanded());
                statsMiniCard.setManaged(task.isStatsExpanded());
                eyeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: " + baseFontSize + "px; -padding: 0 10 0 0; -fx-text-fill: " + (task.isStatsExpanded() ? "#569CD6" : "#AAAAAA") + ";");
                StorageManager.saveTasks(globalDatabase);
            });
            mainRow.getChildren().add(eyeBtn);
        }

        if (!isLocked && !task.isLinkCard()) {
            if (task.getDeadline() != null && !isNoteMode) {
                mainRow.getChildren().add(new TaskDeadlineLabel(task, config, appStats, globalDatabase, onUpdate, activeTimelines, metaFontSize));
            }
            // 6. Append Right-Side Action Controls
            mainRow.getChildren().add(new TaskActionControls(task, config, appStats, globalDatabase, onUpdate, baseFontSize, metaFontSize, isNoteMode));
        }

        // 7. Assemble Card Components
        SubTaskRenderer subTaskBox = new SubTaskRenderer(task, config, appStats, globalDatabase, onUpdate);
        if (isLocked) { subTaskBox.setVisible(false); subTaskBox.setManaged(false); }

        primaryCard.getChildren().addAll(mainRow, subTaskBox);
        getChildren().addAll(primaryCard, statsMiniCard);

        // 8. Context Menu
        ContextMenu contextMenu = TaskContextMenu.build(task, config, appStats, globalDatabase, onUpdate, onGoToPage);
        this.setOnContextMenuRequested(e -> {
            contextMenu.show(this, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private HBox buildMetaBox(TaskItem task, SectionConfig config, AppStats appStats, boolean isLocked, boolean isNoteMode, int baseFontSize, int metaFontSize, List<TaskItem> db, Runnable onUpdate) {
        HBox metaBox = new HBox(7);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        boolean hasLinks = config.isEnableLinks() && task.getTaskLinks() != null && !task.getTaskLinks().isEmpty();
        boolean hasSubTasks = config.isEnableSubTasks() && !task.getSubTasks().isEmpty();
        Button expandBtn = new Button(task.isExpanded() ? "▼" : "▶");
        expandBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-font-weight: bold; -fx-padding: 0 5 0 0; -fx-cursor: hand; -fx-font-size: " + metaFontSize + "px;");
        if (isLocked || (!hasSubTasks && !hasLinks)) { expandBtn.setVisible(false); expandBtn.setManaged(false); }
        expandBtn.setOnAction(e -> { task.setExpanded(!task.isExpanded()); StorageManager.saveTasks(db); onUpdate.run(); });
        metaBox.getChildren().add(expandBtn);

        if (!isLocked) {
            if (task.isOptional()) {
                Label opt = new Label("[OPTIONAL]");
                opt.setStyle("-fx-text-fill: #FFD700; -fx-font-size: " + metaFontSize + "px; -fx-font-weight: bold; -fx-padding: 0 5 0 0;");
                metaBox.getChildren().add(opt);
            }
            Region sideRect = new Region();
            sideRect.setMinWidth(5); sideRect.setPrefWidth(5);
            if (isNoteMode) sideRect.setPrefHeight(30); else { sideRect.setPrefHeight(25); sideRect.setMaxHeight(25); }

            String fillColor = "#FFFFFF";
            if ((isNoteMode || config.isEnableTaskStyling()) && task.getCustomSideboxColor() != null && !task.getCustomSideboxColor().equals("transparent")) fillColor = task.getCustomSideboxColor();
            else if (config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) fillColor = task.getPriority().getColorHex();
            else if (config.isShowPrefix() && appStats.isMatchDailyRectColor() && task.getPrefixColor() != null) fillColor = task.getPrefixColor();
            sideRect.setStyle("-fx-background-color: " + fillColor + "; -fx-background-radius: 3;");

            if (config.isEnableIcons() && task.getIconSymbol() != null && !task.getIconSymbol().equals("None")) {
                Label icon = new Label(task.getIconSymbol());
                icon.setStyle("-fx-text-fill: " + (task.getIconColor() != null ? task.getIconColor() : "#FFFFFF") + "; -fx-font-size: " + (baseFontSize + 2) + "px;");
                metaBox.getChildren().add(icon);
            }
            metaBox.getChildren().add(sideRect);

            if (config.isAllowFavorite() && task.isFavorite() && !isNoteMode) {
                Label star = new Label("[⭐]"); star.setStyle("-fx-text-fill: #FFD700; -fx-font-size: " + baseFontSize + "px; -fx-font-weight: bold;");
                metaBox.getChildren().add(star);
            }
            if (config.isShowDate() && !isNoteMode && !task.isLinkCard()) {
                Label dLabel = new Label("[" + task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "]");
                dLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: " + metaFontSize + "px;");
                metaBox.getChildren().add(dLabel);
            }
            if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) {
                Label pLabel = new Label(task.getPrefix());
                pLabel.setStyle("-fx-text-fill: " + (task.getPrefixColor() != null ? task.getPrefixColor() : "#4EC9B0") + "; -fx-font-size: " + baseFontSize + "px;");
                metaBox.getChildren().add(pLabel);
            }
            if (config.isShowTaskType() && task.getTaskType() != null && !task.getTaskType().isEmpty() && !isNoteMode) {
                Label tLabel = new Label("[" + task.getTaskType() + "]");
                tLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: " + metaFontSize + "px;");
                metaBox.getChildren().add(tLabel);
            }
        }
        return metaBox;
    }

    private HBox buildTextContainer(TaskItem task, boolean isLocked, int baseFontSize, int metaFontSize, boolean isNoteMode, List<String> blockingTaskNames) {
        HBox textContainer = new HBox(5);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        String fontStyle = "-fx-font-size: " + baseFontSize + "px; ";

        if (isLocked) {
            textContainer.setAlignment(Pos.CENTER);
            Label lockIcon = new Label("🔒"); lockIcon.setStyle("-fx-text-fill: #FF6666; -fx-font-size: " + (baseFontSize + 2) + "px;");
            Label lockMsg = new Label("Complete [" + (blockingTaskNames.size() == 1 ? blockingTaskNames.get(0) : blockingTaskNames.size() + " tasks") + "] to unlock");
            lockMsg.setStyle(fontStyle + "-fx-text-fill: #858585; -fx-font-style: italic;");
            textContainer.getChildren().addAll(lockIcon, lockMsg);

            if (blockingTaskNames.size() > 1) {
                Label helpIcon = new Label("?");
                helpIcon.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold; -fx-font-size: " + metaFontSize + "px; -fx-cursor: help; -fx-background-color: #1E1E1E; -fx-padding: 0 5; -fx-background-radius: 10; -fx-border-color: #569CD6; -fx-border-radius: 10;");
                HBox.setMargin(helpIcon, new Insets(0, 0, 0, 5));
                Tooltip.install(helpIcon, new Tooltip("Required Tasks:\n• " + String.join("\n• ", blockingTaskNames)));
                textContainer.getChildren().add(helpIcon);
            }
        } else {
            textContainer.setAlignment(Pos.CENTER_LEFT);
            Label textLabel = new Label(task.getTextContent());
            textLabel.setWrapText(true);
            textLabel.setStyle(fontStyle + "-fx-strikethrough: " + (task.isFinished() && !isNoteMode && !task.isLinkCard()) + "; -fx-text-fill: #E0E0E0;");
            textContainer.getChildren().add(textLabel);

            if (task.isLinkCard()) {
                Label linkInd = new Label("↗");
                linkInd.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold; -fx-font-size: " + metaFontSize + "px; -fx-padding: 0 0 0 5;");
                textContainer.getChildren().add(linkInd);
            }
        }
        return textContainer;
    }
}