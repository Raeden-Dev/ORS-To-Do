package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DeletedHistoryDialog {

    public static void show(AppStats appStats, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Deleted Tasks History");
        dialog.setHeaderText("Log of recently deleted tasks:");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(10);
        content.setPrefSize(450, 350);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(appStats.getDeletedTaskHistory());
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Force dark theme on the ListView cells
        listView.setStyle("-fx-background-color: #2D2D30; -fx-control-inner-background: #2D2D30; -fx-border-color: #555555;");
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2D2D30;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #2D2D30; -fx-text-fill: #E0E0E0;");
                    setWrapText(true);
                }
            }
        });

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button wipeBtn = new Button("🗑 Wipe History");
        wipeBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #FF6666; -fx-border-radius: 3;");

        wipeBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently erase the deleted task history?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Wipe History?");
            TaskDialogs.styleDialog(confirm);

            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    appStats.getDeletedTaskHistory().clear();
                    StorageManager.saveStats(appStats);
                    listView.getItems().clear();
                    if (onUpdate != null) onUpdate.run();
                }
            });
        });

        actionBox.getChildren().add(wipeBtn);
        content.getChildren().addAll(listView, actionBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
}