package com.raeden.ors_to_do.modules.dependencies.ui.dialogs;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.Debuff;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class DebuffManagerDialog {

    public static void show(AppStats appStats, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Debuff Manager"); // --- FIXED: Renamed Dialog ---
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPrefSize(500, 550);
        content.setPadding(new Insets(10));

        // --- FIXED: Dark background, bright blue outline style ---
        Button addBtn = new Button("Create New Debuff Template");
        addBtn.setStyle("-fx-background-color: #1E1E1E; -fx-border-color: #569CD6; -fx-text-fill: #569CD6; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");

        ListView<Debuff> listView = new ListView<>();
        listView.setStyle("-fx-background-color: #1E1E1E; -fx-control-inner-background: #1E1E1E; -fx-border-color: transparent;");
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Remove default blue selection highlighting from the list view
        listView.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(".list-cell:filled:selected:focused, .list-cell:filled:selected { -fx-background-color: transparent; }".getBytes()));

        Runnable refreshList = () -> {
            listView.getItems().clear();
            listView.getItems().addAll(appStats.getDebuffTemplates());
        };
        refreshList.run();

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Debuff item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // --- FIXED: Render as a styled Card ---
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: #252526; -fx-border-color: " + item.getColorHex() + "88; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 12;");

                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label nL = new Label(item.getIconSymbol() + " " + item.getName());
                    nL.setStyle("-fx-text-fill: " + item.getColorHex() + "; -fx-font-weight: bold; -fx-font-size: 14px;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button inflictBtn = new Button("Inflict");
                    inflictBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: #FF6666; -fx-cursor: hand; -fx-font-weight: bold;");
                    inflictBtn.setOnAction(e -> {
                        appStats.getActiveDebuffs().add(item.cloneAsActive());
                        StorageManager.saveStats(appStats);
                        onUpdate.run();
                    });

                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-background-color: #3E3E42; -fx-text-fill: white; -fx-cursor: hand;");
                    editBtn.setOnAction(e -> DebuffEditDialog.show(item, appStats, () -> { StorageManager.saveStats(appStats); refreshList.run(); onUpdate.run(); }));

                    Button delBtn = new Button("❌");
                    delBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand;");
                    delBtn.setOnAction(e -> {
                        appStats.getDebuffTemplates().remove(item);
                        StorageManager.saveStats(appStats);
                        refreshList.run();
                    });

                    header.getChildren().addAll(nL, spacer, inflictBtn, editBtn, delBtn);

                    Label descL = new Label(item.getDescription() == null || item.getDescription().isEmpty() ? "No description." : item.getDescription());
                    descL.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px; -fx-font-style: italic;");
                    descL.setWrapText(true);

                    card.getChildren().addAll(header, descL);

                    setGraphic(card);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5 10 5 10;");
                }
            }
        });

        addBtn.setOnAction(e -> DebuffEditDialog.show(null, appStats, () -> { StorageManager.saveStats(appStats); refreshList.run(); }));

        content.getChildren().addAll(addBtn, listView);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}