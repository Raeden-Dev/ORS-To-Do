package com.raeden.ors_to_do.modules.dependencies.settings;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.StorageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.Collections;

public class TemplateManagerPanel extends VBox {
    private VBox existingTemplatesBox;
    private ComboBox<AppStats.SectionConfig> templateSectionSelector;
    private AppStats appStats;
    private Runnable refreshCallback;

    public TemplateManagerPanel(AppStats appStats, Runnable refreshCallback) {
        super(15);
        this.appStats = appStats;
        this.refreshCallback = refreshCallback;

        setStyle("-fx-border-color: #3E3E42; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        HBox tempTopRow = new HBox(15);
        tempTopRow.setAlignment(Pos.CENTER_LEFT);
        Label templateHeader = new Label("Auto-Generating Tasks");
        templateHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #CCCCCC;");

        // --- FIXED: Initialize the container FIRST before triggering the refresh method ---
        existingTemplatesBox = new VBox(10);

        templateSectionSelector = new ComboBox<>();
        refreshSectionSelector(); // Populates combo box AND renders existing templates

        templateSectionSelector.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(AppStats.SectionConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        templateSectionSelector.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(AppStats.SectionConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });

        templateSectionSelector.setOnAction(e -> renderExistingTemplates());
        tempTopRow.getChildren().addAll(templateHeader, new Label("  For Section:"), templateSectionSelector);

        HBox templateInput = new HBox(10);
        templateInput.setAlignment(Pos.CENTER_LEFT);
        TextField tempPrefix = new TextField(); tempPrefix.setPromptText("[Prefix]"); tempPrefix.setPrefWidth(80);
        ColorPicker prefixColorPicker = new ColorPicker(Color.web("#4EC9B0")); prefixColorPicker.setStyle("-fx-color-label-visible: false;");
        TextField tempText = new TextField(); tempText.setPromptText("Task Content"); HBox.setHgrow(tempText, Priority.ALWAYS);
        ColorPicker bgColorPicker = new ColorPicker(Color.TRANSPARENT); bgColorPicker.setStyle("-fx-color-label-visible: false;");

        Button addTempBtn = new Button("Add Task");
        addTempBtn.setPrefWidth(120);
        addTempBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-border-color: #555555; -fx-cursor: hand;");
        addTempBtn.setOnAction(e -> {
            if(!tempText.getText().isEmpty() && templateSectionSelector.getValue() != null) {
                String cleanPrefix = tempPrefix.getText().trim().toUpperCase();
                if (!cleanPrefix.isEmpty()) {
                    if (!cleanPrefix.startsWith("[")) cleanPrefix = "[" + cleanPrefix;
                    if (!cleanPrefix.endsWith("]")) cleanPrefix = cleanPrefix + "]";
                }
                String pColor = toHexString(prefixColorPicker.getValue());
                String bColor = bgColorPicker.getValue().getOpacity() == 0.0 ? null : toHexString(bgColorPicker.getValue());

                templateSectionSelector.getValue().getAutoAddTemplates().add(new AppStats.DailyTemplate(cleanPrefix, tempText.getText().trim(), pColor, bColor));
                StorageManager.saveStats(appStats);
                renderExistingTemplates();
                tempPrefix.clear(); tempText.clear();
            }
        });

        templateInput.getChildren().addAll(tempPrefix, prefixColorPicker, tempText, new Label("Bg:"), bgColorPicker, addTempBtn);
        getChildren().addAll(tempTopRow, existingTemplatesBox, new Separator(), templateInput);
    }

    public void refreshSectionSelector() {
        AppStats.SectionConfig currentSelection = templateSectionSelector.getValue();
        templateSectionSelector.getItems().clear();
        templateSectionSelector.getItems().addAll(appStats.getSections());

        if (currentSelection != null && appStats.getSections().contains(currentSelection)) {
            templateSectionSelector.setValue(currentSelection);
        } else if (!appStats.getSections().isEmpty()) {
            templateSectionSelector.setValue(appStats.getSections().get(0));
        }
        renderExistingTemplates();
    }

    private void renderExistingTemplates() {
        if (existingTemplatesBox == null) return; // Extra safety check
        existingTemplatesBox.getChildren().clear();

        AppStats.SectionConfig activeConfig = templateSectionSelector.getValue();
        if(activeConfig == null) return;

        for (int i = 0; i < activeConfig.getAutoAddTemplates().size(); i++) {
            AppStats.DailyTemplate temp = activeConfig.getAutoAddTemplates().get(i);
            int index = i;

            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5));
            if (temp.getBgColor() != null) row.setStyle("-fx-background-color: " + temp.getBgColor() + "; -fx-background-radius: 3;");

            Label prefixLabel = new Label(temp.getPrefix() != null ? temp.getPrefix() : "");
            prefixLabel.setStyle("-fx-text-fill: " + temp.getPrefixColor() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
            prefixLabel.setPrefWidth(80);

            Label textLabel = new Label(temp.getText());
            textLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox btnBox = new HBox(5);

            Button upBtn = new Button("▲");
            upBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            upBtn.setDisable(index == 0);
            upBtn.setOnAction(e -> {
                Collections.swap(activeConfig.getAutoAddTemplates(), index, index - 1);
                StorageManager.saveStats(appStats);
                renderExistingTemplates();
            });

            Button downBtn = new Button("▼");
            downBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-cursor: hand;");
            downBtn.setDisable(index == activeConfig.getAutoAddTemplates().size() - 1);
            downBtn.setOnAction(e -> {
                Collections.swap(activeConfig.getAutoAddTemplates(), index, index + 1);
                StorageManager.saveStats(appStats);
                renderExistingTemplates();
            });

            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Auto-Task");
                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);

                TextField preF = new TextField(temp.getPrefix());
                ColorPicker preC = new ColorPicker(Color.web(temp.getPrefixColor()));
                TextField txtF = new TextField(temp.getText());
                ColorPicker bgC = new ColorPicker(temp.getBgColor() != null ? Color.web(temp.getBgColor()) : Color.TRANSPARENT);

                Button clearBgBtn = new Button("Clear");
                clearBgBtn.setOnAction(ev -> bgC.setValue(Color.TRANSPARENT));
                HBox bgBox = new HBox(5, bgC, clearBgBtn);

                grid.add(new Label("Prefix:"), 0, 0); grid.add(preF, 1, 0);
                grid.add(new Label("Prefix Color:"), 0, 1); grid.add(preC, 1, 1);
                grid.add(new Label("Content:"), 0, 2); grid.add(txtF, 1, 2);
                grid.add(new Label("BG Color:"), 0, 3); grid.add(bgBox, 1, 3);

                dialog.getDialogPane().setContent(grid);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        temp.setPrefix(preF.getText().trim());
                        temp.setText(txtF.getText().trim());
                        temp.setPrefixColor(toHexString(preC.getValue()));
                        temp.setBgColor(bgC.getValue().getOpacity() == 0.0 ? null : toHexString(bgC.getValue()));
                        StorageManager.saveStats(appStats);
                        renderExistingTemplates();
                    }
                });
            });

            Button removeBtn = new Button("Remove");
            removeBtn.setStyle("-fx-background-color: #552222; -fx-text-fill: white; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> {
                activeConfig.getAutoAddTemplates().remove(temp);
                StorageManager.saveStats(appStats);
                renderExistingTemplates();
            });

            btnBox.getChildren().addAll(upBtn, downBtn, editBtn, removeBtn);
            row.getChildren().addAll(prefixLabel, textLabel, spacer, btnBox);
            existingTemplatesBox.getChildren().add(row);
        }
    }

    private String toHexString(Color color) {
        if (color == null) return null;
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}