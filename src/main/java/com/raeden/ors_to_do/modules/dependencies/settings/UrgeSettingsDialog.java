package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;

public class UrgeSettingsDialog {

    public static void show(AppStats appStats, Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Urge Surfing Settings");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        content.setPrefWidth(500);

        CheckBox enableCheck = new CheckBox("Enable Urge Surfing Button in Focus Hub");
        enableCheck.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        enableCheck.setSelected(appStats.isEnableUrgeButton());

        Spinner<Integer> durationSpinner = new Spinner<>(30, 3600, appStats.getUrgeSessionDurationSeconds());
        durationSpinner.setEditable(true);
        HBox durationBox = new HBox(10, new Label("Breathing Session Duration (Seconds):"), durationSpinner);
        durationBox.setStyle("-fx-alignment: center-left;");

        content.getChildren().addAll(enableCheck, durationBox, new Separator());

        Label quotesHeader = new Label("Inspirational Quotes / Mantras:");
        quotesHeader.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        content.getChildren().add(quotesHeader);

        ListView<String> quoteList = new ListView<>();
        quoteList.getItems().addAll(appStats.getUrgeQuotes());
        quoteList.setPrefHeight(200);
        quoteList.setStyle("-fx-background-color: #1E1E1E; -fx-control-inner-background: #2D2D30; -fx-text-fill: white;");

        TextField newQuoteField = new TextField();
        newQuoteField.setPromptText("Enter a new quote or mantra...");
        HBox.setHgrow(newQuoteField, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            String text = newQuoteField.getText().trim();
            if (!text.isEmpty()) {
                quoteList.getItems().add(text);
                newQuoteField.clear();
            }
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
            String selected = quoteList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                quoteList.getItems().remove(selected);
            }
        });

        HBox inputBox = new HBox(10, newQuoteField, addBtn, removeBtn);
        content.getChildren().addAll(quoteList, inputBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                appStats.setEnableUrgeButton(enableCheck.isSelected());
                appStats.setUrgeSessionDurationSeconds(durationSpinner.getValue());
                appStats.setUrgeQuotes(new ArrayList<>(quoteList.getItems()));
                onSave.run();
            }
        });
    }
}