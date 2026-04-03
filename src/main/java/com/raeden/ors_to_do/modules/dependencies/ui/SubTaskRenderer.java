package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskLink;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class SubTaskRenderer extends VBox {

    public SubTaskRenderer(TaskItem task, SectionConfig config, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        super(8);
        setPadding(new Insets(0, 10, 15, 60));

        boolean hasLinks = config.isEnableLinks() && task.getTaskLinks() != null && !task.getTaskLinks().isEmpty();
        boolean hasSubTasks = config.isEnableSubTasks() && !task.getSubTasks().isEmpty();

        if (hasLinks || hasSubTasks) {
            setVisible(task.isExpanded());
            setManaged(task.isExpanded());
        } else {
            setVisible(false);
            setManaged(false);
            return; // Nothing to render!
        }

        if (hasLinks) {
            for (TaskLink linkObj : task.getTaskLinks()) {
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
                getChildren().add(linkRow);
            }
        }

        if (hasSubTasks) {
            for (SubTask sub : task.getSubTasks()) {
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
                getChildren().add(subRow);
            }
        }
    }
}