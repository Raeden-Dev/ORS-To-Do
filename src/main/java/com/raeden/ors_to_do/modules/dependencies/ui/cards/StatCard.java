package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class StatCard extends VBox {

    public StatCard(CustomStat stat, AppStats appStats, Runnable onUpdate) {
        super();

        String bgColor = stat.getBackgroundColor() != null && !stat.getBackgroundColor().equals("transparent") ? stat.getBackgroundColor() : "#2D2D30";
        String txtColor = stat.getTextColor() != null && !stat.getTextColor().equals("transparent") ? stat.getTextColor() : "#FFFFFF";

        HBox mainRow = new HBox(10);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(10, 15, 10, 10));

        String defaultStyle = "-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-border-color: " + txtColor + "44; -fx-border-radius: 5;";
        String hoverStyle = "-fx-background-color: " + bgColor + "; -fx-background-radius: 5; -fx-border-color: " + txtColor + "; -fx-border-radius: 5;";
        mainRow.setStyle(defaultStyle);

        if (stat.getIconSymbol() != null && !stat.getIconSymbol().equals("None") && !stat.getIconSymbol().isEmpty()) {
            Label iconLabel = new Label(stat.getIconSymbol());
            iconLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px;");
            mainRow.getChildren().add(iconLabel);
        }

        Rectangle sideBar = new Rectangle(4, 20, Color.web(txtColor));
        sideBar.setArcWidth(3);
        sideBar.setArcHeight(3);
        mainRow.getChildren().add(sideBar);

        Label nameLabel = new Label(stat.getName());
        nameLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        mainRow.getChildren().add(nameLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        mainRow.getChildren().add(spacer);

        Button helpBtn = new Button("?");
        helpBtn.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: " + txtColor + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: " + txtColor + "88; -fx-border-radius: 50; -fx-background-radius: 50; -fx-min-width: 20px; -fx-min-height: 20px; -fx-max-width: 20px; -fx-max-height: 20px; -fx-padding: 0;");
        helpBtn.setOnAction(e -> showStatDescriptionDialog(stat, txtColor, bgColor));

        Label amountLabel = new Label(String.valueOf(stat.getCurrentAmount()));
        amountLabel.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        int effectiveMax = stat.getEffectiveMaxCap(appStats.getActiveDebuffs());
        Label maxLabel = new Label("/ " + effectiveMax);
        maxLabel.setStyle("-fx-text-fill: #858585; -fx-font-size: 12px; -fx-padding: 3 0 0 0;");

        mainRow.getChildren().addAll(helpBtn, amountLabel, maxLabel);
        getChildren().add(mainRow);

        mainRow.setOnMouseEntered(e -> mainRow.setStyle(hoverStyle));
        mainRow.setOnMouseExited(e -> mainRow.setStyle(defaultStyle));
    }

    private void showStatDescriptionDialog(CustomStat stat, String txtColor, String bgColor) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Stat Info: " + stat.getName());
        alert.setHeaderText(stat.getIconSymbol() + " " + stat.getName());

        String content = stat.getDescription() != null && !stat.getDescription().isEmpty() ? stat.getDescription() : "No description provided for this stat.";
        content += "\n\nLifetime Earned: " + stat.getLifetimeEarned();
        content += "\nLifetime Lost: " + stat.getLifetimeLost();
        content += "\nMax Level Reached: " + stat.getMaxLevelReached();

        alert.setContentText(content);
        TaskDialogs.styleDialog(alert);
        alert.show();
    }
}