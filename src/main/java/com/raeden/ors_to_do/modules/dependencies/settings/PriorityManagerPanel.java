package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.Collections;

public class PriorityManagerPanel extends VBox {
    private VBox existingPriosBox;
    private AppStats appStats;
    private Runnable refreshCallback;

    public PriorityManagerPanel(AppStats appStats, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.refreshCallback = refreshCallback;

        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label prioHeader = new Label("Manage Priorities");
        prioHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        existingPriosBox = new VBox(10);
        renderExistingPriorities();

        // --- FIXED: 1-Row Layout with Dark Theme applied ---
        HBox prioInput = new HBox(10);
        prioInput.setAlignment(Pos.CENTER_LEFT);

        TextField prioName = new TextField();
        prioName.setPromptText("Priority Name (e.g. URGENT)");
        prioName.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: white; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3;");
        HBox.setHgrow(prioName, Priority.ALWAYS); // Widens the text field dynamically

        ColorPicker colorPicker = new ColorPicker(Color.WHITE);
        colorPicker.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #555555; -fx-border-radius: 3; -fx-background-radius: 3; -fx-color-label-visible: false;");

        Button addPrioBtn = new Button("Add Priority");
        addPrioBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 15;");

        addPrioBtn.setOnAction(e -> {
            if(!prioName.getText().trim().isEmpty()) {
                appStats.getCustomPriorities().add(new CustomPriority(prioName.getText().trim(), toHexString(colorPicker.getValue())));
                StorageManager.saveStats(appStats);
                renderExistingPriorities();
                refreshCallback.run();
                prioName.clear();
            }
        });

        prioInput.getChildren().addAll(prioName, colorPicker, addPrioBtn);

        getChildren().addAll(prioHeader, existingPriosBox, new Separator(), prioInput);
    }

    private void renderExistingPriorities() {
        existingPriosBox.getChildren().clear();

        for (int i = 0; i < appStats.getCustomPriorities().size(); i++) {
            CustomPriority prio = appStats.getCustomPriorities().get(i);
            int index = i;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(prio.getName());
            nameLabel.setStyle("-fx-text-fill: " + prio.getColorHex() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            nameLabel.setPrefWidth(150);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox btnBox = new HBox(5);

            Button upBtn = new Button("▲");
            upBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
            upBtn.setDisable(index == 0);
            upBtn.setOnAction(e -> {
                Collections.swap(appStats.getCustomPriorities(), index, index - 1);
                StorageManager.saveStats(appStats);
                renderExistingPriorities();
                refreshCallback.run();
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #AAAAAA; -fx-cursor: hand; -fx-border-color: #3E3E42; -fx-border-radius: 3;");
            downBtn.setDisable(index == appStats.getCustomPriorities().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(appStats.getCustomPriorities(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingPriorities();
                refreshCallback.run();
            });

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Priority");

                TaskDialogs.styleDialog(dialog);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);

                TextField nameF = new TextField(prio.getName());
                ColorPicker colC = new ColorPicker(Color.web(prio.getColorHex()));

                grid.add(new Label("Name:"), 0, 0); grid.add(nameF, 1, 0);
                grid.add(new Label("Color:"), 0, 1); grid.add(colC, 1, 1);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK && !nameF.getText().trim().isEmpty()) {
                        appStats.getCustomPriorities().set(index, new CustomPriority(nameF.getText().trim(), toHexString(colC.getValue())));
                        StorageManager.saveStats(appStats);
                        renderExistingPriorities();
                        refreshCallback.run();
                    }
                });
            });

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                if (appStats.getCustomPriorities().size() <= 1) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "You must have at least one priority left in the system.");
                    TaskDialogs.styleDialog(alert);
                    alert.setHeaderText(null); alert.show();
                    return;
                }
                appStats.getCustomPriorities().remove(prio);
                StorageManager.saveStats(appStats);
                renderExistingPriorities();
                refreshCallback.run();
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(nameLabel, spacer, btnBox);
            existingPriosBox.getChildren().add(row);
        }
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}