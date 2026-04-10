package com.raeden.ors_to_do.modules.dependencies.ui.utils;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class TaskCardStyleHelper {

    public static String getBaseStyle(TaskItem task, SectionConfig config, AppStats appStats, boolean isLocked) {
        boolean isNoteMode = config != null && config.isNotesPage();
        boolean allowStyling = isNoteMode || (config != null && config.isEnableTaskStyling());

        String bgStyle = "";
        String borderStyle = "";
        String textVars = "";

        // Determine Background Color
        if (task.getColorHex() != null && !task.getColorHex().equals("transparent")) {
            if (isLocked) {
                // Dynamically darken the specific hex color by 60%
                bgStyle = "-fx-background-color: derive(" + task.getColorHex() + ", -60%); ";
            } else {
                bgStyle = "-fx-background-color: " + task.getColorHex() + "; ";
            }
        } else if (isLocked) {
            // Default dark background for locked tasks with no custom color
            bgStyle = "-fx-background-color: #151515; ";
        }

        // Determine Outline Color / Special Card Overrides
        if (task.isOptional()) {
            bgStyle = isLocked ? "-fx-background-color: derive(#332B00, -60%); " : "-fx-background-color: #332B00; ";
            borderStyle = "-fx-border-color: " + (isLocked ? "derive(#FFD700, -60%)" : "#FFD700") + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (task.isLinkCard()) {
            bgStyle = isLocked ? "-fx-background-color: derive(#1A3A4D, -60%); " : "-fx-background-color: #1A3A4D; ";
            borderStyle = "-fx-border-color: " + (isLocked ? "derive(#569CD6, -60%)" : "#569CD6") + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (allowStyling && task.getCustomOutlineColor() != null && !task.getCustomOutlineColor().equals("transparent")) {
            borderStyle = "-fx-border-color: " + (isLocked ? "derive(" + task.getCustomOutlineColor() + ", -60%)" : task.getCustomOutlineColor()) + "; -fx-border-width: 1; -fx-border-radius: 4; ";
        } else if (!isLocked) {
            if (config != null && config.isAllowFavorite() && task.isFavorite()) {
                borderStyle = "-fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 4; ";
            } else if (appStats.isMatchPriorityOutline() && config != null && config.isShowPriority() && task.getPriority() != null && task.getPriority().getColorHex() != null) {
                borderStyle = "-fx-border-color: " + task.getPriority().getColorHex() + "; -fx-border-width: 1; -fx-border-radius: 4; ";
            }
        } else {
            // Give locked tasks a subtle, dimmed outline if they don't have a custom one
            borderStyle = "-fx-border-color: #2D2D30; -fx-border-width: 1; -fx-border-radius: 4; ";
        }

        // --- FIXED: Define text variables and apply specific darkening if locked ---
        if (!isLocked) {
            // Define base colors that other elements can derive from
            textVars = "-fx-base-text-color: #E0E0E0; -fx-meta-text-color: #858585; ";
        } else {
            // Darken both normal text and meta/muted text colors
            textVars = "-fx-base-text-color: derive(#E0E0E0, -50%); -fx-meta-text-color: derive(#858585, -50%); ";
        }

        return bgStyle + borderStyle + textVars;
    }

    public static void setupDragAndDrop(VBox cardNode, VBox primaryCard, TaskItem task, String originalStyle, BiConsumer<String, String> onReorder) {
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