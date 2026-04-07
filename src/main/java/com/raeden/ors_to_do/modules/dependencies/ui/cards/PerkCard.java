package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerkCard extends VBox {
    private boolean isExpanded = false;

    public PerkCard(TaskItem perkTask, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        super(10);

        boolean meetsRequirements = true;
        VBox requirementsBox = new VBox(5);
        requirementsBox.setPadding(new Insets(10, 0, 0, 0));

        // 1. Check Stat Requirements
        for (Map.Entry<String, Integer> req : perkTask.getStatRequirements().entrySet()) {
            CustomStat foundStat = appStats.getCustomStats().stream().filter(s -> s.getId().equals(req.getKey())).findFirst().orElse(null);
            if (foundStat != null) {
                if (foundStat.getCurrentAmount() < req.getValue()) {
                    meetsRequirements = false;
                    Label l = new Label("❌ Requires " + req.getValue() + " " + foundStat.getName() + " (Current: " + foundStat.getCurrentAmount() + ")");
                    l.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 12px;");
                    requirementsBox.getChildren().add(l);
                } else {
                    Label l = new Label("✅ " + req.getValue() + " " + foundStat.getName());
                    l.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 12px;");
                    requirementsBox.getChildren().add(l);
                }
            }
        }

        // 2. Check Perk (Task) Dependencies
        if (perkTask.getDependsOnTaskIds() != null && !perkTask.getDependsOnTaskIds().isEmpty()) {
            for (String depId : perkTask.getDependsOnTaskIds()) {
                TaskItem depTask = globalDatabase.stream().filter(t -> t.getId().equals(depId)).findFirst().orElse(null);
                if (depTask != null) {
                    boolean isDepUnlocked = depTask.getPerkLevel() > 0 || depTask.isFinished() || (depTask.getStatRequirements().isEmpty() && (depTask.getDependsOnTaskIds() == null || depTask.getDependsOnTaskIds().isEmpty()));

                    if (!isDepUnlocked) {
                        meetsRequirements = false;
                        Label l = new Label("❌ Requires Perk: " + depTask.getTextContent());
                        l.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 12px;");
                        requirementsBox.getChildren().add(l);
                    } else {
                        Label l = new Label("✅ Hooked Perk: " + depTask.getTextContent() + " Unlocked");
                        l.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 12px;");
                        requirementsBox.getChildren().add(l);
                    }
                }
            }
        }

        // --- 15-Minute Setup Phase Logic ---
        LocalDateTime creationTime = perkTask.getDateCreated();
        boolean isSetupPhase = LocalDateTime.now().isBefore(creationTime.plusMinutes(15));
        boolean isLocked = !meetsRequirements || isSetupPhase;

        if (isSetupPhase) {
            long minsLeft = Duration.between(LocalDateTime.now(), creationTime.plusMinutes(15)).toMinutes();
            Label setupLbl = new Label("⏳ Inactive (Setup Phase) - " + (minsLeft + 1) + " mins left");
            setupLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold;");
            requirementsBox.getChildren().add(0, setupLbl);
        }

        // --- Tracking Unlock and Lost Dates ---
        boolean stateChanged = false;

        if (meetsRequirements && !isSetupPhase && perkTask.getPerkUnlockedDate() == null) {
            perkTask.setPerkUnlockedDate(LocalDateTime.now());
            perkTask.setPerkLostDate(null);
            if (perkTask.getPerkLevel() == 0) perkTask.setPerkLevel(1);
            stateChanged = true;
        }
        else if (!meetsRequirements && perkTask.getPerkUnlockedDate() != null) {
            perkTask.setPerkLostDate(LocalDateTime.now());
            perkTask.setPerkUnlockedDate(null);
            perkTask.setPerkLevel(0); // Reset perk level upon loss
            stateChanged = true;
        }

        if (stateChanged) {
            StorageManager.saveTasks(globalDatabase);
        }

        // --- Display Dates in UI ---
        if (perkTask.getPerkUnlockedDate() != null) {
            Label unlockedLbl = new Label("📅 Unlocked: " + perkTask.getPerkUnlockedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            unlockedLbl.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 11px;");
            requirementsBox.getChildren().add(unlockedLbl);
        }
        if (perkTask.getPerkLostDate() != null) {
            Label lostLbl = new Label("⚠️ Lost: " + perkTask.getPerkLostDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            lostLbl.setStyle("-fx-text-fill: #E06666; -fx-font-size: 11px;");
            requirementsBox.getChildren().add(lostLbl);
        }

        // Dynamic Custom Colors
        String bgColor = perkTask.getColorHex() != null && !perkTask.getColorHex().equals("transparent") ? perkTask.getColorHex() : "#2D2D30";
        String outlineColor = perkTask.getCustomOutlineColor() != null && !perkTask.getCustomOutlineColor().equals("transparent") ? perkTask.getCustomOutlineColor() : "#569CD6";
        String iconColor = perkTask.getIconColor() != null && !perkTask.getIconColor().equals("transparent") ? perkTask.getIconColor() : "#FFFFFF";
        String iconStr = (perkTask.getIconSymbol() != null && !perkTask.getIconSymbol().equals("None")) ? perkTask.getIconSymbol() + " " : "✨ ";

        // Visual Styling & Level Glow based on custom colors
        if (!isLocked && perkTask.getPerkLevel() > 0) {
            int glowSpread = perkTask.getPerkLevel() * 8; // Max level 5 = spread 40
            setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: " + outlineColor + "; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, " + outlineColor + ", " + glowSpread + ", 0.3, 0, 0);");
        } else if (!isLocked) {
            setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: " + outlineColor + "; -fx-border-radius: 5;");
        } else {
            setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-opacity: 0.7;");
        }

        // --- HEADER ---
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label((isLocked ? "🔒 " : iconStr) + perkTask.getTextContent());
        nameLabel.setStyle("-fx-text-fill: " + (isLocked ? "#858585" : iconColor) + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label levelLabel = new Label("Level " + perkTask.getPerkLevel());
        levelLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #332B00; -fx-padding: 3 8; -fx-background-radius: 10;");
        if (isLocked) levelLabel.setVisible(false);

        Button editBtn = new Button("⚙");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openPerkConfigDialog(perkTask, appStats, globalDatabase, onUpdate));

        header.getChildren().addAll(nameLabel, spacer, levelLabel, editBtn);

        // --- DESCRIPTION (Expandable) ---
        VBox descBox = new VBox(5);
        descBox.setVisible(false);
        descBox.setManaged(false);
        Label descLabel = new Label(perkTask.getPerkDescription() == null || perkTask.getPerkDescription().isEmpty() ? "No description." : perkTask.getPerkDescription());
        descLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px; -fx-font-style: italic;");
        descLabel.setWrapText(true);
        descBox.getChildren().add(descLabel);

        Button toggleDescBtn = new Button("▼ Show Details");
        toggleDescBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + outlineColor + "; -fx-cursor: hand; -fx-padding: 0;");
        toggleDescBtn.setOnAction(e -> {
            isExpanded = !isExpanded;
            descBox.setVisible(isExpanded);
            descBox.setManaged(isExpanded);
            toggleDescBtn.setText(isExpanded ? "▲ Hide Details" : "▼ Show Details");
        });

        getChildren().addAll(header, toggleDescBtn, descBox);
        if (!requirementsBox.getChildren().isEmpty()) {
            getChildren().addAll(new Separator(), requirementsBox);
        }
    }

    private void openPerkConfigDialog(TaskItem perkTask, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configure Perk: " + perkTask.getTextContent());
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // 1. Name & Description
        TextField nameInput = new TextField(perkTask.getTextContent());
        nameInput.setPromptText("Perk Name");

        TextArea descInput = new TextArea(perkTask.getPerkDescription() != null ? perkTask.getPerkDescription() : "");
        descInput.setPromptText("What does this perk do? (e.g. Grants access to special tasks, buffs a stat, etc.)");
        descInput.setPrefRowCount(3);

        content.getChildren().addAll(new Label("Perk Name:"), nameInput, new Label("Perk Effect / Lore Description:"), descInput);

        // 2. Styling & Icon Section
        content.getChildren().add(new Separator());
        Label styleLabel = new Label("Perk Appearance & Styling:");
        styleLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");
        content.getChildren().add(styleLabel);

        GridPane styleGrid = new GridPane();
        styleGrid.setHgap(15); styleGrid.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        styleGrid.getColumnConstraints().addAll(col1, col2);

        ComboBox<String> iconBox = new ComboBox<>();
        iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
        iconBox.setValue(perkTask.getIconSymbol() != null ? perkTask.getIconSymbol() : "None");
        iconBox.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(iconBox, Priority.ALWAYS);

        ColorPicker iconColorPicker = new ColorPicker(Color.web(perkTask.getIconColor() != null ? perkTask.getIconColor() : "#FFFFFF"));
        iconColorPicker.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(iconColorPicker, Priority.ALWAYS);

        ColorPicker bgColorPicker = new ColorPicker(Color.web(perkTask.getColorHex() != null && !perkTask.getColorHex().equals("transparent") ? perkTask.getColorHex() : "#2D2D30"));
        ColorPicker outlinePicker = new ColorPicker(Color.web(perkTask.getCustomOutlineColor() != null && !perkTask.getCustomOutlineColor().equals("transparent") ? perkTask.getCustomOutlineColor() : "#569CD6"));

        bgColorPicker.setMaxWidth(Double.MAX_VALUE);
        outlinePicker.setMaxWidth(Double.MAX_VALUE);

        styleGrid.add(new Label("Icon & Color:"), 0, 0);
        styleGrid.add(new HBox(10, iconBox, iconColorPicker), 1, 0);

        styleGrid.add(new Label("Background Color:"), 0, 1);
        styleGrid.add(bgColorPicker, 1, 1);

        styleGrid.add(new Label("Outline & Glow Color:"), 0, 2);
        styleGrid.add(outlinePicker, 1, 2);

        Button randomBtn = new Button("🎲 Randomize Style");
        randomBtn.setMaxWidth(Double.MAX_VALUE);
        randomBtn.setOnAction(e -> {
            java.util.Random rand = new java.util.Random();
            double hue = rand.nextDouble() * 360.0;
            iconBox.setValue(TaskDialogs.ICON_LIST[rand.nextInt(TaskDialogs.ICON_LIST.length - 1) + 1]);
            iconColorPicker.setValue(Color.hsb(hue, 0.5, 0.95));
            bgColorPicker.setValue(Color.hsb(hue, 0.8, 0.2));
            outlinePicker.setValue(Color.hsb(hue, 0.8, 0.8));
        });
        styleGrid.add(randomBtn, 1, 3);
        content.getChildren().add(styleGrid);

        // 3. Stat Requirements
        content.getChildren().add(new Separator());
        Label hookLabel = new Label("Hook Stat Requirements (RPG Thresholds):");
        hookLabel.setStyle("-fx-text-fill: #4EC9B0; -fx-font-weight: bold;");
        content.getChildren().add(hookLabel);

        HBox statInputBox = new HBox(10);
        ComboBox<CustomStat> statBox = new ComboBox<>();
        statBox.getItems().addAll(appStats.getCustomStats());
        statBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(CustomStat item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        statBox.setButtonCell(statBox.getCellFactory().call(null));
        statBox.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(statBox, Priority.ALWAYS);

        Spinner<Integer> amountSpinner = new Spinner<>(1, 99999, 100);
        amountSpinner.setEditable(true);
        amountSpinner.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(amountSpinner, Priority.ALWAYS);

        Button addStatBtn = new Button("Add Hook");
        addStatBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white;");

        statInputBox.getChildren().addAll(statBox, amountSpinner, addStatBtn);
        content.getChildren().add(statInputBox);

        VBox activeReqsBox = new VBox(5);

        Runnable[] refreshReqs = new Runnable[1];
        refreshReqs[0] = () -> {
            activeReqsBox.getChildren().clear();
            for (Map.Entry<String, Integer> req : perkTask.getStatRequirements().entrySet()) {
                CustomStat s = appStats.getCustomStats().stream().filter(x -> x.getId().equals(req.getKey())).findFirst().orElse(null);
                if (s != null) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);

                    Label l = new Label("• Requires " + req.getValue() + " " + s.getName());
                    l.setStyle("-fx-text-fill: #E0E0E0;");

                    Button removeBtn = new Button("❌");
                    removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand;");
                    removeBtn.setOnAction(e -> {
                        perkTask.getStatRequirements().remove(req.getKey());
                        refreshReqs[0].run();
                    });

                    row.getChildren().addAll(l, removeBtn);
                    activeReqsBox.getChildren().add(row);
                }
            }
        };
        refreshReqs[0].run(); // Initial population

        addStatBtn.setOnAction(e -> {
            if (statBox.getValue() != null) {
                perkTask.getStatRequirements().put(statBox.getValue().getId(), amountSpinner.getValue());
                refreshReqs[0].run();
            }
        });

        content.getChildren().add(activeReqsBox);

        // 4. Hook Tasks / Challenges
        content.getChildren().add(new Separator());
        Label depLabel = new Label("Hook Tasks / Challenges:");
        depLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        content.getChildren().add(depLabel);

        MenuButton dependenciesMenu = new MenuButton("Select Parent Requirements...");
        dependenciesMenu.getStyleClass().add("custom-menu-btn");
        dependenciesMenu.setMaxWidth(Double.MAX_VALUE);
        List<String> selectedDeps = new ArrayList<>(perkTask.getDependsOnTaskIds());
        int[] depCount = {0};

        Map<String, Menu> sectionMenus = new HashMap<>();
        if (appStats != null && appStats.getSections() != null) {
            for (SectionConfig sc : appStats.getSections()) {
                Menu m = new Menu(sc.getName());
                sectionMenus.put(sc.getId(), m);
                dependenciesMenu.getItems().add(m);
            }
        }
        Menu othersMenu = new Menu("Other Tasks");

        for (TaskItem other : globalDatabase) {
            if (other.getId().equals(perkTask.getId()) || other.isArchived()) continue;

            CheckBox cb = new CheckBox(other.getTextContent());
            cb.setStyle("-fx-text-fill: white;");
            cb.setSelected(selectedDeps.contains(other.getId()));
            if (cb.isSelected()) depCount[0]++;

            cb.setOnAction(e -> {
                if (cb.isSelected() && !selectedDeps.contains(other.getId())) selectedDeps.add(other.getId());
                else if (!cb.isSelected()) selectedDeps.remove(other.getId());
                dependenciesMenu.setText("Hooked Requirements (" + selectedDeps.size() + ")");
            });

            CustomMenuItem item = new CustomMenuItem(cb);
            item.setHideOnClick(false);

            Menu targetMenu = sectionMenus.get(other.getSectionId());
            if (targetMenu != null) {
                targetMenu.getItems().add(item);
            } else {
                othersMenu.getItems().add(item);
            }
        }

        // Clean up empty folders
        dependenciesMenu.getItems().removeIf(menuItem -> menuItem instanceof Menu && ((Menu) menuItem).getItems().isEmpty());
        if (!othersMenu.getItems().isEmpty()) dependenciesMenu.getItems().add(othersMenu);

        dependenciesMenu.setText("Hooked Requirements (" + depCount[0] + ")");
        if (dependenciesMenu.getItems().isEmpty()) {
            CustomMenuItem emptyItem = new CustomMenuItem(new Label("No other tasks available."));
            emptyItem.setDisable(true);
            dependenciesMenu.getItems().add(emptyItem);
        }

        content.getChildren().add(dependenciesMenu);

        // Wrap in ScrollPane to ensure it never gets cramped
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 650);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                perkTask.setTextContent(nameInput.getText().trim());
                perkTask.setPerkDescription(descInput.getText().trim());

                perkTask.setIconSymbol(iconBox.getValue());
                perkTask.setIconColor(toHexString(iconColorPicker.getValue()));
                perkTask.setColorHex(toHexString(bgColorPicker.getValue()));
                perkTask.setCustomOutlineColor(toHexString(outlinePicker.getValue()));

                perkTask.setDependsOnTaskIds(selectedDeps);

                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    private String toHexString(Color color) {
        if (color == null || color.getOpacity() == 0.0) return "transparent";
        return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }
}