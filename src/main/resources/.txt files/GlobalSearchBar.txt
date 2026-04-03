package com.raeden.ors_to_do.modules.dependencies.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

public class GlobalSearchBar extends StackPane {
    private TextField searchField;
    private Label clearSearchBtn;

    public GlobalSearchBar(Consumer<String> onSearchChanged) {
        setAlignment(Pos.CENTER_RIGHT);

        searchField = new TextField();
        searchField.setPromptText("🔍 Search...");
        searchField.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white; -fx-prompt-text-fill: #858585; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8 40 8 8;");

        clearSearchBtn = new Label("✖");
        String normalStyle = "-fx-text-fill: #858585; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 8 12 8 12;";
        String hoverStyle = "-fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 14px; -fx-padding: 8 12 8 12;";

        clearSearchBtn.setStyle(normalStyle);
        clearSearchBtn.setVisible(false);

        clearSearchBtn.setOnMouseClicked(e -> clear());
        clearSearchBtn.setOnMouseEntered(e -> clearSearchBtn.setStyle(hoverStyle));
        clearSearchBtn.setOnMouseExited(e -> clearSearchBtn.setStyle(normalStyle));

        getChildren().addAll(searchField, clearSearchBtn);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearSearchBtn.setVisible(newVal != null && !newVal.isEmpty());
            onSearchChanged.accept(newVal);
        });
    }

    public void clear() {
        searchField.clear();
    }

    public boolean isEmpty() {
        return searchField.getText() == null || searchField.getText().trim().isEmpty();
    }
}