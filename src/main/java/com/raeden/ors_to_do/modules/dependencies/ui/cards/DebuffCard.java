package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.Debuff;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDateTime;

public class DebuffCard extends VBox {

    public DebuffCard(Debuff debuff, AppStats appStats, Runnable onUpdate) {
        super(5);
        setStyle("-fx-background-color: " + debuff.getColorHex() + "33; -fx-border-color: " + debuff.getColorHex() + "; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        setPrefWidth(220);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(debuff.getIconSymbol());
        iconLbl.setStyle("-fx-text-fill: " + debuff.getColorHex() + "; -fx-font-size: 16px;");

        Label nameLbl = new Label(debuff.getName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(iconLbl, nameLbl, spacer);

        if (debuff.isAura()) {
            Label auraLbl = new Label("AURA");
            auraLbl.setStyle("-fx-text-fill: #C586C0; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-color: #331A33; -fx-padding: 2 6; -fx-background-radius: 5; -fx-border-color: #C586C0; -fx-border-radius: 5;");
            header.getChildren().add(auraLbl);
        } else if (debuff.isAllowStacking() && debuff.getCurrentStacks() > 1) {
            Label stackLbl = new Label("x" + debuff.getCurrentStacks());
            stackLbl.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: #1A0000; -fx-padding: 2 6; -fx-background-radius: 5;");
            header.getChildren().add(stackLbl);
        }
        getChildren().add(header);

        if (debuff.getDescription() != null && !debuff.getDescription().isEmpty()) {
            Label descLbl = new Label(debuff.getDescription());
            descLbl.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 11px;");
            descLbl.setWrapText(true);
            getChildren().add(descLbl);
        }

        if (!debuff.isAura()) {
            if (debuff.getRequiredTaskCompletions() > 0) {
                Label reqLbl = new Label("Tasks: " + debuff.getCurrentTaskCompletions() + " / " + debuff.getRequiredTaskCompletions());
                reqLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 11px; -fx-font-weight: bold;");
                getChildren().add(reqLbl);
            }

            if (debuff.getExpiryDate() != null) {
                Duration d = Duration.between(LocalDateTime.now(), debuff.getExpiryDate());
                long hours = d.toHours();
                Label timeLbl = new Label("Expires in: " + (hours > 0 ? hours + "h" : d.toMinutes() + "m"));
                timeLbl.setStyle("-fx-text-fill: #569CD6; -fx-font-size: 11px; -fx-font-weight: bold;");
                getChildren().add(timeLbl);
            }
        }

        Tooltip t = new Tooltip();
        StringBuilder sb = new StringBuilder("Effects:\n");
        appStats.getCustomStats().forEach(s -> {
            if (debuff.getStatGainMultipliers().containsKey(s.getId())) {
                double multi = debuff.getStatGainMultipliers().get(s.getId());
                if (debuff.isAllowStacking() && debuff.getStatGainMultiplierStackReductions().containsKey(s.getId())) {
                    multi -= debuff.getStatGainMultiplierStackReductions().get(s.getId()) * (debuff.getCurrentStacks() - 1);
                }
                sb.append("• ").append(s.getName()).append(" XP gain reduced to ").append(Math.max(0, Math.round(multi * 100))).append("%\n");
            }
            if (debuff.getStatCapReductions().containsKey(s.getId())) {
                int reduction = debuff.getStatCapReductions().get(s.getId());
                if (debuff.isAllowStacking() && debuff.getStatCapReductionStackIncreasers().containsKey(s.getId())) {
                    reduction += debuff.getStatCapReductionStackIncreasers().get(s.getId()) * (debuff.getCurrentStacks() - 1);
                }
                sb.append("• ").append(s.getName()).append(" Max Cap lowered by ").append(reduction).append("\n");
            }
        });
        if (debuff.isAura()) {
            sb.append("\n[This is an Aura. It can only be removed by improving the associated Stat.]");
        }
        t.setText(sb.toString().trim());
        Tooltip.install(this, t);

        this.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                if (debuff.isAura()) {
                    Alert a = new Alert(Alert.AlertType.WARNING, "This debuff is an Aura bound to a Stat Threshold.\n\nYou cannot remove it manually. You must improve the corresponding stat to break the Aura.");
                    a.setHeaderText("Aura Locked");
                    TaskDialogs.styleDialog(a);
                    a.show();
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to manually remove the '" + debuff.getName() + "' debuff early?", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Remove Debuff");
                TaskDialogs.styleDialog(confirm);

                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        appStats.getActiveDebuffs().remove(debuff);
                        StorageManager.saveStats(appStats);
                        onUpdate.run();
                    }
                });
            }
        });
    }
}