package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.Collections;

public class StatsManagerPanel extends VBox {
    private VBox existingStatsBox;
    private AppStats appStats;
    private Runnable refreshCallback;
    private final double BUTTON_WIDTH = 200.0;

    // --- FIXED: Extracted label to class level so we can update it dynamically ---
    private Label descLabel;

    public StatsManagerPanel(AppStats appStats, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.refreshCallback = refreshCallback;

        setStyle("-fx-border-color: #B5CEA8; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");
        Label header = new Label("Stats Configuration (RPG System)");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #B5CEA8;");

        descLabel = new Label();

        existingStatsBox = new VBox(10);
        renderExistingStats();

        Button createStatBtn = new Button("+ Create New Stat");
        createStatBtn.setPrefWidth(BUTTON_WIDTH);
        createStatBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        createStatBtn.setOnAction(e -> showStatDialog(null));

        getChildren().addAll(header, descLabel, existingStatsBox, new Separator(), createStatBtn);

        // Run the initial state check
        refreshState();
    }

    // --- NEW: Public method to update the UI instantly when settings change ---
    public void refreshState() {
        if (!appStats.isGlobalStatsEnabled()) {
            this.setDisable(true);
            descLabel.setText("⚠️ Custom Stats are disabled. Turn them on in General Configuration to use this feature.");
            descLabel.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            this.setDisable(false);
            descLabel.setText("Create custom stats (Strength, Focus, etc.) to attach to your tasks.");
            descLabel.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px;");
        }
    }

    private void renderExistingStats() {
        existingStatsBox.getChildren().clear();

        for (int i = 0; i < appStats.getCustomStats().size(); i++) {
            CustomStat stat = appStats.getCustomStats().get(i);
            int index = i;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #2D2D30; -fx-padding: 10; -fx-border-color: #3E3E42; -fx-border-radius: 5;");

            Label badgePreview = new Label((stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") ? stat.getIconSymbol() + " " : "") + stat.getName());
            String bgColor = stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333";
            String txtColor = stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF";
            badgePreview.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + txtColor + "; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-weight: bold;");
            badgePreview.setPrefWidth(150);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox btnBox = new HBox(5);

            Button upBtn = new Button("▲");
            upBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
            upBtn.setDisable(index == 0);
            upBtn.setOnAction(e -> {
                Collections.swap(appStats.getCustomStats(), index, index - 1);
                StorageManager.saveStats(appStats);
                renderExistingStats();
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
            downBtn.setDisable(index == appStats.getCustomStats().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(appStats.getCustomStats(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingStats();
            });

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showStatDialog(stat));

            Button removeBtn = new Button("❌");
            removeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete stat '" + stat.getName() + "'? This will remove it from future use.", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText("Delete Custom Stat");
                TaskDialogs.styleDialog(alert);
                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getCustomStats().remove(stat);
                        StorageManager.saveStats(appStats);
                        renderExistingStats();
                    }
                });
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(badgePreview, spacer, btnBox);
            existingStatsBox.getChildren().add(row);
        }
    }

    private void showStatDialog(CustomStat stat) {
        boolean isNew = (stat == null);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Create Custom Stat" : "Edit Stat: " + stat.getName());
        TaskDialogs.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        int rowIdx = 0;

        TextField nameField = new TextField(isNew ? "" : stat.getName());
        nameField.setPromptText("e.g. Strength, Intellect");
        nameField.setMaxWidth(Double.MAX_VALUE);
        grid.add(new Label("Stat Name:"), 0, rowIdx);
        grid.add(nameField, 1, rowIdx++);

        ComboBox<String> iconBox = new ComboBox<>();
        iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
        iconBox.setValue(!isNew && stat.getIconSymbol() != null ? stat.getIconSymbol() : "None");
        iconBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(new Label("Icon Symbol:"), 0, rowIdx);
        grid.add(iconBox, 1, rowIdx++);

        ColorPicker bgColorPicker = new ColorPicker(Color.web(!isNew && stat.getBackgroundColor() != null ? stat.getBackgroundColor() : "#333333"));
        bgColorPicker.setMaxWidth(Double.MAX_VALUE);
        grid.add(new Label("Background Color:"), 0, rowIdx);
        grid.add(bgColorPicker, 1, rowIdx++);

        ColorPicker textColorPicker = new ColorPicker(Color.web(!isNew && stat.getTextColor() != null ? stat.getTextColor() : "#FFFFFF"));
        textColorPicker.setMaxWidth(Double.MAX_VALUE);
        grid.add(new Label("Text Color:"), 0, rowIdx);
        grid.add(textColorPicker, 1, rowIdx++);

        Button randomBtn = new Button("🎲 Randomize Style");
        randomBtn.setMaxWidth(Double.MAX_VALUE);
        randomBtn.setOnAction(e -> {
            java.util.Random rand = new java.util.Random();
            double hue = rand.nextDouble() * 360.0;
            iconBox.setValue(TaskDialogs.ICON_LIST[rand.nextInt(TaskDialogs.ICON_LIST.length - 1) + 1]);
            bgColorPicker.setValue(Color.hsb(hue, 1.0, 0.2));
            textColorPicker.setValue(Color.hsb(hue, 0.6, 0.95));
        });
        grid.add(randomBtn, 1, rowIdx++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !nameField.getText().trim().isEmpty()) {
                CustomStat target = isNew ? new CustomStat() : stat;

                target.setName(nameField.getText().trim());
                target.setIconSymbol(iconBox.getValue());
                target.setBackgroundColor(toHexString(bgColorPicker.getValue()));
                target.setTextColor(toHexString(textColorPicker.getValue()));

                if (isNew) {
                    appStats.getCustomStats().add(target);
                }

                StorageManager.saveStats(appStats);
                renderExistingStats();
            }
        });
    }

    private String toHexString(Color color) {
        if (color == null || color.getOpacity() == 0.0) return "transparent";
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}