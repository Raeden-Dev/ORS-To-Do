package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StatCard extends VBox {

    public StatCard(CustomStat stat, AppStats appStats, Runnable onUpdate) {
        super();

        // Dynamic Styling from Config
        String bgColor = stat.getBackgroundColor() != null && !stat.getBackgroundColor().equals("transparent") ? stat.getBackgroundColor() : "#2D2D30";
        String txtColor = stat.getTextColor() != null && !stat.getTextColor().equals("transparent") ? stat.getTextColor() : "#FFFFFF";

        // Main Row Container mimicking TaskCard
        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10, 15, 10, 10));

        // Apply background and border styling
        String defaultStyle = "-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: " + txtColor + "; -fx-border-width: 1;";
        String hoverStyle = "-fx-background-color: derive(" + bgColor + ", 10%); -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: " + txtColor + "; -fx-border-width: 1;";
        mainRow.setStyle(defaultStyle);

        // 1. Icon (Added first, before the sidebar)
        if (stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") && !stat.getIconSymbol().isEmpty()) {
            Label iconLabel = new Label(stat.getIconSymbol());
            iconLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px;");
            mainRow.getChildren().add(iconLabel);
        }

        // 2. Left Side Color Bar
        Rectangle sideBar = new Rectangle(4, 20, Color.web(txtColor));
        sideBar.setArcWidth(3);
        sideBar.setArcHeight(3);
        mainRow.getChildren().add(sideBar);

        // 3. Name
        Label nameLabel = new Label(stat.getName());
        nameLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px;");
        mainRow.getChildren().add(nameLabel);

        // 4. Spacer to push the amount to the far right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        mainRow.getChildren().add(spacer);

        // 5. Right Side: Just the raw amount number
        Label amountLabel = new Label(String.valueOf(stat.getCurrentAmount()));
        amountLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 5 0 0;");
        mainRow.getChildren().add(amountLabel);

        getChildren().add(mainRow);

        // Subtle hover effect mimicking TaskCard
        mainRow.setOnMouseEntered(e -> mainRow.setStyle(hoverStyle));
        mainRow.setOnMouseExited(e -> mainRow.setStyle(defaultStyle));
    }
}