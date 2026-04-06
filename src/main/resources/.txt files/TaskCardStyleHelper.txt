package com.raeden.ors_to_do.modules.dependencies.ui.utils;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.TaskCard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class TaskCardStyleHelper {

    public static String getBaseStyle(TaskItem task, SectionConfig config, AppStats appStats, boolean isLocked) {
        boolean isNoteMode = config.isNotesPage();
        boolean allowStyling = isNoteMode || config.isEnableTaskStyling();

        String bgStyle = task.getColorHex() != null && !task.getColorHex().equals("transparent") ? "-fx-background-color: " + task.getColorHex() + "; " : "";
        String borderStyle = "";

        if (task.isOptional()) {
            bgStyle = "-fx-background-color: #332B00; ";
            borderStyle = "-fx-border-color: #FFD700; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (task.isLinkCard()) {
            bgStyle = "-fx-background-color: #1A3A4D; ";
            borderStyle = "-fx-border-color: #569CD6; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (allowStyling && task.getCustomOutlineColor() != null && !task.getCustomOutlineColor().equals("transparent")) {
            borderStyle = "-fx-border-color: " + task.getCustomOutlineColor() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (!isLocked) {
            if (config.isAllowFavorite() && task.isFavorite()) {
                borderStyle = "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 4; ";
            } else if (appStats.isMatchPriorityOutline() && config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
                borderStyle = "-fx-border-color: " + task.getPriority().getColorHex() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
            }
        }
        return bgStyle + borderStyle;
    }

    public static void setupDragAndDrop(TaskCard cardNode, VBox primaryCard, TaskItem task, String originalStyle, BiConsumer<String, String> onReorder) {
        if (onReorder == null) return;

        cardNode.setOnDragDetected(event -> {
            Dragboard db = cardNode.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(task.getId());
            db.setContent(content);
            event.consume();
        });
        cardNode.setOnDragOver(event -> {
            if (event.getGestureSource() != cardNode && event.getDragboard().hasString()) event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        cardNode.setOnDragEntered(event -> {
            if (event.getGestureSource() != cardNode && event.getDragboard().hasString())
                primaryCard.setStyle(originalStyle + " -fx-border-color: #569CD6; -fx-border-width: 2;");
        });
        cardNode.setOnDragExited(event -> primaryCard.setStyle(originalStyle));
        cardNode.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                onReorder.accept(db.getString(), task.getId());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}