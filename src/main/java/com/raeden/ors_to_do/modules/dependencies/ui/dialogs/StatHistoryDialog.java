package com.raeden.ors_to_do.modules.dependencies.ui.dialogs;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.Debuff;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatHistoryDialog {

    public static void show(AppStats appStats, List<TaskItem> globalDatabase) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recent RPG History");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        boolean hasHistory = false;
        String baseBadgeStyle = "-fx-padding: 2 6; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 11px;";

        // Iterate backward to show most recent tasks first
        for (int i = globalDatabase.size() - 1; i >= 0; i--) {
            TaskItem task = globalDatabase.get(i);

            boolean hasRewards = task.getStatRewards() != null && !task.getStatRewards().isEmpty();
            boolean hasCapRewards = task.getStatCapRewards() != null && !task.getStatCapRewards().isEmpty();
            boolean hasCosts = task.getStatCosts() != null && !task.getStatCosts().isEmpty();
            boolean hasPenalties = task.getStatPenalties() != null && !task.getStatPenalties().isEmpty();
            boolean hasDebuffs = task.getInflictedDebuffIds() != null && !task.getInflictedDebuffIds().isEmpty();

            // --- FIXED: Correctly determine if a task was missed/failed using available flags ---
            boolean isMissed = task.isPenaltyApplied() || (task.isArchived() && !task.isFinished());

            String dateStr = "Unknown Date";
            if (task.getDateCompleted() != null) {
                dateStr = task.getDateCompleted().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            } else if (task.getPerkUnlockedDate() != null) {
                dateStr = task.getPerkUnlockedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            } else if (task.getDateCreated() != null) {
                dateStr = task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            }

            // ==========================================
            // 1. GAINS CARD (Task Completed Successfully)
            // ==========================================
            if (task.isFinished() && (hasRewards || hasCapRewards || hasCosts)) {
                hasHistory = true;

                VBox entryBox = new VBox(5);
                entryBox.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #4EC9B0; -fx-border-width: 1;");

                Label sourceLabel = new Label("✔️ Completed: " + task.getTextContent());
                sourceLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label dateLabel = new Label("Date: " + dateStr);
                dateLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");

                FlowPane badgesBox = new FlowPane(5, 5);

                if (hasRewards) {
                    for (Map.Entry<String, Integer> entry : task.getStatRewards().entrySet()) {
                        CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(entry.getKey())).findFirst().orElse(null);
                        if (stat != null) {
                            Label lbl = new Label("+" + entry.getValue() + " " + stat.getName());
                            lbl.setStyle("-fx-text-fill: #4EC9B0; -fx-background-color: #1A332E; " + baseBadgeStyle);
                            badgesBox.getChildren().add(lbl);
                        }
                    }
                }

                if (hasCapRewards) {
                    for (Map.Entry<String, Integer> entry : task.getStatCapRewards().entrySet()) {
                        CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(entry.getKey())).findFirst().orElse(null);
                        if (stat != null) {
                            Label lbl = new Label("▲ " + entry.getValue() + " Max " + stat.getName());
                            lbl.setStyle("-fx-text-fill: #C586C0; -fx-background-color: #2D1E2D; " + baseBadgeStyle);
                            badgesBox.getChildren().add(lbl);
                        }
                    }
                }

                if (hasCosts) {
                    for (Map.Entry<String, Integer> entry : task.getStatCosts().entrySet()) {
                        CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(entry.getKey())).findFirst().orElse(null);
                        if (stat != null) {
                            Label lbl = new Label("~" + entry.getValue() + " " + stat.getName());
                            lbl.setStyle("-fx-text-fill: #FF8C00; -fx-background-color: #331C00; " + baseBadgeStyle);
                            badgesBox.getChildren().add(lbl);
                        }
                    }
                }

                entryBox.getChildren().addAll(sourceLabel, dateLabel, badgesBox);
                content.getChildren().add(entryBox);
            }

            // ==========================================
            // 2. LOSSES CARD (Task Missed / Failed)
            // ==========================================
            if (isMissed && (hasPenalties || hasDebuffs)) {
                hasHistory = true;

                VBox entryBox = new VBox(5);
                // Deep red background and border for failure cards
                entryBox.setStyle("-fx-background-color: #331A1A; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #E06666; -fx-border-width: 1;");

                Label sourceLabel = new Label("❌ Missed: " + task.getTextContent());
                sourceLabel.setStyle("-fx-text-fill: #E06666; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label dateLabel = new Label("Date: " + dateStr);
                dateLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");

                FlowPane badgesBox = new FlowPane(5, 5);

                if (hasPenalties) {
                    for (Map.Entry<String, Integer> entry : task.getStatPenalties().entrySet()) {
                        CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(entry.getKey())).findFirst().orElse(null);
                        if (stat != null) {
                            Label lbl = new Label("-" + entry.getValue() + " " + stat.getName());
                            lbl.setStyle("-fx-text-fill: #E06666; -fx-background-color: #4A1A1A; " + baseBadgeStyle);
                            badgesBox.getChildren().add(lbl);
                        }
                    }
                }

                if (hasDebuffs && appStats.getDebuffTemplates() != null) {
                    for (String dId : task.getInflictedDebuffIds()) {
                        Debuff d = appStats.getDebuffTemplates().stream().filter(db -> db.getId().equals(dId)).findFirst().orElse(null);
                        if (d != null) {
                            String iconText = (d.getIconSymbol() != null && !d.getIconSymbol().equals("None")) ? d.getIconSymbol() + " " : "⚠ ";
                            String color = d.getColorHex() != null && !d.getColorHex().equals("transparent") ? d.getColorHex() : "#FF4444";

                            Label lbl = new Label("Inflicted: " + iconText + d.getName());
                            lbl.setStyle("-fx-text-fill: " + color + "; -fx-background-color: derive(" + color + ", -80%); -fx-border-color: " + color + "; -fx-border-radius: 5; " + baseBadgeStyle);
                            badgesBox.getChildren().add(lbl);
                        }
                    }
                }

                entryBox.getChildren().addAll(sourceLabel, dateLabel, badgesBox);
                content.getChildren().add(entryBox);
            }
        }

        if (!hasHistory) {
            Label empty = new Label("No recent tasks found that modified RPG stats or debuffs.");
            empty.setStyle("-fx-text-fill: #858585; -fx-font-style: italic;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(480, 500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scroll.setBorder(Border.EMPTY);

        String scrollCss = ".scroll-bar:vertical { -fx-background-color: transparent; -fx-pref-width: 5; } " +
                ".scroll-bar:vertical .track { -fx-background-color: transparent; -fx-border-color: transparent; } " +
                ".scroll-bar:vertical .thumb { -fx-background-color: #555555; -fx-background-radius: 5; }";
        scroll.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(scrollCss.getBytes()));

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}