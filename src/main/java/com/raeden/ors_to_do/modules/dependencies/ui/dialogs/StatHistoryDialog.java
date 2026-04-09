package com.raeden.ors_to_do.modules.dependencies.ui.dialogs;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatHistoryDialog {

    public static void show(AppStats appStats, List<TaskItem> globalDatabase) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Recent Stat Gains History");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        boolean hasHistory = false;
        for (int i = globalDatabase.size() - 1; i >= 0; i--) {
            TaskItem task = globalDatabase.get(i);

            if (task.isFinished() && task.getStatRewards() != null && !task.getStatRewards().isEmpty()) {
                hasHistory = true;

                String dateStr = "Unknown Date";
                if (task.getPerkUnlockedDate() != null) dateStr = task.getPerkUnlockedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                else if (task.getDateCreated() != null) dateStr = task.getDateCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

                VBox entryBox = new VBox(5);
                entryBox.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-color: #3E3E42;");

                Label sourceLabel = new Label("From: " + task.getTextContent());
                sourceLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label dateLabel = new Label("Completed: " + dateStr);
                dateLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 11px;");

                HBox rewardsBox = new HBox(10);
                for (Map.Entry<String, Integer> reward : task.getStatRewards().entrySet()) {
                    CustomStat stat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(reward.getKey())).findFirst().orElse(null);
                    if (stat != null) {
                        Label rLbl = new Label("+" + reward.getValue() + " " + stat.getName());
                        rLbl.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold; -fx-background-color: #1A332E; -fx-padding: 2 6; -fx-background-radius: 5;");
                        rewardsBox.getChildren().add(rLbl);
                    }
                }

                entryBox.getChildren().addAll(sourceLabel, dateLabel, rewardsBox);
                content.getChildren().add(entryBox);
            }
        }

        if (!hasHistory) {
            Label empty = new Label("No recent tasks found that granted custom stats.");
            empty.setStyle("-fx-text-fill: #858585; -fx-font-style: italic;");
            content.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(450, 500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scroll.setBorder(Border.EMPTY);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}