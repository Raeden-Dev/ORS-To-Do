package com.raeden.ors_to_do.modules.dependencies.ui.dialogs;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.Debuff;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class DebuffEditDialog {

    public static void show(Debuff template, AppStats appStats, Runnable onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(template == null ? "Create Debuff" : "Edit Debuff");
        TaskDialogs.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);
        grid.getColumnConstraints().addAll(new ColumnConstraints(150), new ColumnConstraints(250));

        int r = 0;
        TextField nameF = new TextField(template != null ? template.getName() : "");
        grid.add(new Label("Debuff Name:"), 0, r); grid.add(nameF, 1, r++);

        TextField descF = new TextField(template != null ? template.getDescription() : "");
        grid.add(new Label("Description:"), 0, r); grid.add(descF, 1, r++);

        ComboBox<String> iconBox = new ComboBox<>();
        iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
        iconBox.setValue(template != null ? template.getIconSymbol() : "☠");
        ColorPicker colorPicker = new ColorPicker(Color.web(template != null ? template.getColorHex() : "#8B0000"));
        HBox iconRow = new HBox(10, iconBox, colorPicker);
        grid.add(new Label("Icon & Color:"), 0, r); grid.add(iconRow, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);

        Spinner<Integer> tasksSp = new Spinner<>(0, 999, template != null ? template.getRequiredTaskCompletions() : 5);
        tasksSp.setEditable(true);
        grid.add(new Label("Tasks to Cleanse:"), 0, r); grid.add(tasksSp, 1, r++);

        Spinner<Integer> hoursSp = new Spinner<>(0, 8760, template != null ? template.getDurationHours() : 0);
        hoursSp.setEditable(true);
        grid.add(new Label("Hours to Auto-Expire (0=Never):"), 0, r); grid.add(hoursSp, 1, r++);

        grid.add(new Separator(), 0, r++, 2, 1);
        Label modL = new Label("Stat Penalties (While Active):"); modL.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold;");
        grid.add(modL, 0, r++, 2, 1);

        Map<String, TextField> multiFields = new HashMap<>();
        Map<String, TextField> capFields = new HashMap<>();

        GridPane statGrid = new GridPane();
        statGrid.setHgap(10); statGrid.setVgap(10);
        statGrid.add(new Label("Gain Multiplier %"), 1, 0); statGrid.add(new Label("- Max Cap"), 2, 0);

        int sr = 1;
        for (CustomStat stat : appStats.getCustomStats()) {
            Label sl = new Label(stat.getName()); sl.setStyle("-fx-text-fill: " + stat.getTextColor() + ";");
            statGrid.add(sl, 0, sr);

            TextField multi = new TextField(); multi.setPrefWidth(60);
            if (template != null && template.getStatGainMultipliers().containsKey(stat.getId())) {
                multi.setText(String.valueOf(Math.round(template.getStatGainMultipliers().get(stat.getId()) * 100)));
            } else multi.setText("100");
            multiFields.put(stat.getId(), multi); statGrid.add(multi, 1, sr);

            TextField cap = new TextField(); cap.setPrefWidth(60);
            if (template != null && template.getStatCapReductions().containsKey(stat.getId())) {
                cap.setText(String.valueOf(template.getStatCapReductions().get(stat.getId())));
            } else cap.setText("0");
            capFields.put(stat.getId(), cap); statGrid.add(cap, 2, sr);

            sr++;
        }
        grid.add(statGrid, 0, r++, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true); scroll.setPrefSize(450, 500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !nameF.getText().trim().isEmpty()) {
                Debuff d = template != null ? template : new Debuff();
                d.setName(nameF.getText().trim());
                d.setDescription(descF.getText().trim());
                d.setIconSymbol(iconBox.getValue());
                d.setColorHex(TaskDialogs.toHexString(colorPicker.getValue()));
                d.setRequiredTaskCompletions(tasksSp.getValue());
                d.setDurationHours(hoursSp.getValue());

                Map<String, Double> m = new HashMap<>();
                Map<String, Integer> c = new HashMap<>();
                for (CustomStat s : appStats.getCustomStats()) {
                    try { double p = Double.parseDouble(multiFields.get(s.getId()).getText().trim()) / 100.0; if (p >= 0 && p < 1.0) m.put(s.getId(), p); } catch(Exception ignore){}
                    try { int v = Integer.parseInt(capFields.get(s.getId()).getText().trim()); if (v > 0) c.put(s.getId(), v); } catch(Exception ignore){}
                }
                d.setStatGainMultipliers(m); d.setStatCapReductions(c);

                if (template == null) appStats.getDebuffTemplates().add(d);
                onSave.run();
            }
        });
    }
}