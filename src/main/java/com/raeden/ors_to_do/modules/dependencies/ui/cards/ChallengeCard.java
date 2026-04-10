package com.raeden.ors_to_do.modules.dependencies.ui.cards;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.modules.dependencies.ui.dialogs.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.ui.utils.TaskActionHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ChallengeCard extends VBox {
    private boolean isExpanded = false;

    public ChallengeCard(TaskItem challengeTask, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        super(10);

        boolean isCompleted = challengeTask.isFinished();
        boolean meetsRequirements = true;
        VBox requirementsBox = new VBox(5);
        requirementsBox.setPadding(new Insets(5, 0, 0, 0));

        // 1. Check Stat Requirements (Unlock Conditions)
        for (Map.Entry<String, Integer> req : challengeTask.getStatRequirements().entrySet()) {
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

        // 2. Check Hooked Dependencies
        if (challengeTask.getDependsOnTaskIds() != null && !challengeTask.getDependsOnTaskIds().isEmpty()) {
            for (String depId : challengeTask.getDependsOnTaskIds()) {
                TaskItem depTask = globalDatabase.stream().filter(t -> t.getId().equals(depId)).findFirst().orElse(null);
                if (depTask != null) {
                    boolean isDepUnlocked = depTask.isFinished() || depTask.getPerkLevel() > 0;
                    if (!isDepUnlocked) {
                        meetsRequirements = false;
                        Label l = new Label("❌ Requires: " + depTask.getTextContent());
                        l.setStyle("-fx-text-fill: #FF6666; -fx-font-size: 12px;");
                        requirementsBox.getChildren().add(l);
                    } else {
                        Label l = new Label("✅ Hooked: " + depTask.getTextContent());
                        l.setStyle("-fx-text-fill: #4EC9B0; -fx-font-size: 12px;");
                        requirementsBox.getChildren().add(l);
                    }
                }
            }
        }

        // 3. Deadline Check
        boolean isExpired = false;
        if (challengeTask.getDeadline() != null && !isCompleted) {
            isExpired = LocalDateTime.now().isAfter(challengeTask.getDeadline());
            if (isExpired) meetsRequirements = false;
        }

        // 4. Setup Phase & Locking
        LocalDateTime creationTime = challengeTask.getDateCreated();
        boolean isSetupPhase = LocalDateTime.now().isBefore(creationTime.plusMinutes(15));
        boolean isLocked = (!meetsRequirements || isSetupPhase) && !isCompleted;

        // --- STATUS BOX (Always Visible) ---
        VBox statusBox = new VBox(5);
        if (isSetupPhase && !isCompleted) {
            long minsLeft = Duration.between(LocalDateTime.now(), creationTime.plusMinutes(15)).toMinutes();
            Label setupLbl = new Label("⏳ Inactive (Setup Phase) - " + (minsLeft + 1) + " mins left");
            setupLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold;");
            statusBox.getChildren().add(setupLbl);
        }

        if (isCompleted && challengeTask.getPerkUnlockedDate() != null) {
            Label compLbl = new Label("🏆 Conquered on: " + challengeTask.getPerkUnlockedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
            compLbl.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold;");
            statusBox.getChildren().add(compLbl);
        }

        // Dynamic Custom Colors
        String bgColor = challengeTask.getColorHex() != null && !challengeTask.getColorHex().equals("transparent") ? challengeTask.getColorHex() : "#2D2D30";
        String outlineColor = challengeTask.getCustomOutlineColor() != null && !challengeTask.getCustomOutlineColor().equals("transparent") ? challengeTask.getCustomOutlineColor() : "#FF8C00";
        String iconColor = challengeTask.getIconColor() != null && !challengeTask.getIconColor().equals("transparent") ? challengeTask.getIconColor() : "#FFFFFF";
        String iconStr = (challengeTask.getIconSymbol() != null && !challengeTask.getIconSymbol().equals("None")) ? challengeTask.getIconSymbol() + " " : "⚔️ ";

        // Visual Styling & Glow
        if (isCompleted) {
            setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: " + outlineColor + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, " + outlineColor + ", 10, 0.2, 0, 0);");
        } else if (!isLocked) {
            setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: " + outlineColor + "; -fx-border-radius: 5;");
        } else {
            setStyle("-fx-background-color: #1E1E1E; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #3E3E42; -fx-border-radius: 5; -fx-opacity: 0.7;");
        }

        // --- CHECK EDIT TIME LOCK FOR UI ---
        int lockHours = appStats.getPreventEditingHours();
        boolean isTimeLocked = lockHours > 0 && LocalDateTime.now().isAfter(challengeTask.getDateCreated().plusHours(lockHours));

        // --- DOUBLE CLICK EDIT BLOCK ---
        this.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                if (isCompleted) return;

                if (isTimeLocked) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "This challenge was created over " + lockHours + " hour(s) ago and is locked from editing.");
                    alert.setHeaderText("Editing Locked");
                    TaskDialogs.styleDialog(alert);
                    alert.show();
                } else {
                    openChallengeConfigDialog(challengeTask, appStats, globalDatabase, onUpdate);
                }
                e.consume();
            }
        });

        // --- HEADER ---
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label((isLocked ? "🔒 " : iconStr) + challengeTask.getTextContent());
        nameLabel.setStyle("-fx-text-fill: " + (isLocked ? "#858585" : iconColor) + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-strikethrough: " + isCompleted + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (challengeTask.getDeadline() != null && !isCompleted) {
            Label timeLbl = new Label();
            Duration duration = Duration.between(LocalDateTime.now(), challengeTask.getDeadline());

            if (isExpired) {
                timeLbl.setText("Expired!");
                timeLbl.setStyle("-fx-text-fill: #FF4444; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: #331A1A; -fx-padding: 2 6; -fx-background-radius: 3; -fx-border-color: #FF4444; -fx-border-radius: 3;");
            } else {
                long days = duration.toDays();
                long hours = duration.toHours() % 24;
                long minutes = duration.toMinutes() % 60;

                if (days > 0) timeLbl.setText("Expires in " + days + "d " + hours + "h");
                else timeLbl.setText("Expires in " + hours + "h " + minutes + "m");

                timeLbl.setStyle("-fx-text-fill: #E0E0E0; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-color: #3E3E42; -fx-padding: 2 6; -fx-background-radius: 3;");
            }
            header.getChildren().add(timeLbl);
        }

        Label typeLabel = new Label("CHALLENGE");
        typeLabel.setStyle("-fx-text-fill: " + outlineColor + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: " + outlineColor + "; -fx-border-radius: 3; -fx-padding: 2 5;");

        // --- ⚙ SETTINGS BUTTON LOCK ---
        Button editBtn = new Button("⚙");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAAAA; -fx-cursor: hand;");

        if (isCompleted || isTimeLocked) {
            editBtn.setDisable(true);
            if (isTimeLocked) editBtn.setTooltip(new Tooltip("Editing Locked (Time expired)"));
        } else {
            editBtn.setOnAction(e -> openChallengeConfigDialog(challengeTask, appStats, globalDatabase, onUpdate));
        }

        header.getChildren().addAll(nameLabel, spacer, typeLabel, editBtn);

        // --- DESCRIPTION (Always visible) ---
        VBox descBox = new VBox(5);
        Label descLabel = new Label(challengeTask.getPerkDescription() == null || challengeTask.getPerkDescription().isEmpty() ? "No description." : challengeTask.getPerkDescription());
        descLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 13px; -fx-font-style: italic;");
        descLabel.setWrapText(true);
        descBox.getChildren().add(descLabel);

        // --- LOOT BOX ---
        FlowPane lootBox = new FlowPane(10, 5);
        lootBox.setAlignment(Pos.CENTER_LEFT);
        boolean hasLoot = false;

        if (challengeTask.getRewardPoints() > 0) {
            Label l = new Label("+" + challengeTask.getRewardPoints() + " Global Pts");
            l.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #332B00; -fx-padding: 2 6; -fx-background-radius: 5;");
            lootBox.getChildren().add(l);
            hasLoot = true;
        }

        for (CustomStat s : appStats.getCustomStats()) {
            int rVal = challengeTask.getStatRewards().getOrDefault(s.getId(), 0);
            int cVal = challengeTask.getStatCapRewards().getOrDefault(s.getId(), 0);

            String txtColor = s.getTextColor() != null ? s.getTextColor() : "#4EC9B0";

            if (rVal > 0) {
                Label l = new Label("+" + rVal + " " + s.getName() + " XP");
                l.setStyle("-fx-text-fill: " + txtColor + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #1E1E1E; -fx-padding: 2 6; -fx-background-radius: 5;");
                lootBox.getChildren().add(l);
                hasLoot = true;
            }
            if (cVal > 0) {
                Label l = new Label("+" + cVal + " " + s.getName() + " Cap");
                l.setStyle("-fx-text-fill: #C586C0; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #1E1E1E; -fx-padding: 2 6; -fx-background-radius: 5;");
                lootBox.getChildren().add(l);
                hasLoot = true;
            }
        }

        // --- EXPANDABLE REQS & REWARDS ---
        VBox expandableBox = new VBox(10);
        expandableBox.setVisible(false);
        expandableBox.setManaged(false);
        expandableBox.setPadding(new Insets(5, 0, 0, 0));

        boolean hasReqs = !requirementsBox.getChildren().isEmpty();
        if (hasReqs) {
            Label reqTitle = new Label("📋 Requirements:");
            reqTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 11px;");
            VBox reqWrap = new VBox(5, reqTitle, requirementsBox);
            expandableBox.getChildren().add(reqWrap);
        }

        if (hasLoot) {
            if (hasReqs) expandableBox.getChildren().add(new Separator());
            Label lootTitle = new Label("🎁 Rewards:");
            lootTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 11px;");
            VBox lootWrap = new VBox(5, lootTitle, lootBox);
            expandableBox.getChildren().add(lootWrap);
        }

        // --- MAIN LAYOUT ASSEMBLY ---
        getChildren().addAll(header);
        if (!statusBox.getChildren().isEmpty()) getChildren().add(statusBox);
        getChildren().add(descBox);

        if (hasReqs || hasLoot) {
            Button toggleExpandBtn = new Button("▼ Show Requirements & Rewards");
            toggleExpandBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + outlineColor + "; -fx-cursor: hand; -fx-padding: 0;");
            toggleExpandBtn.setOnAction(e -> {
                isExpanded = !isExpanded;
                expandableBox.setVisible(isExpanded);
                expandableBox.setManaged(isExpanded);
                toggleExpandBtn.setText(isExpanded ? "▲ Hide Requirements & Rewards" : "▼ Show Requirements & Rewards");
            });
            getChildren().addAll(toggleExpandBtn, expandableBox);
        }

        // --- COMPLETE BUTTON ---
        if (!isCompleted && !isLocked) {
            Button completeBtn = new Button("Challenge Done");
            completeBtn.setStyle("-fx-background-color: #3A0A0A; -fx-border-color: #E06666; -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: #E06666; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");
            completeBtn.setMaxWidth(Double.MAX_VALUE);
            completeBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you have completed this challenge?\n\nThis is permanent. You will gain the rewards, and this card will be locked forever.", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Challenge Done");
                TaskDialogs.styleDialog(confirm);
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        challengeTask.setFinished(true);
                        challengeTask.setPointsClaimed(true);
                        challengeTask.setPermaLock(true);
                        challengeTask.setPerkUnlockedDate(LocalDateTime.now());

                        TaskActionHandler.processRPGStats(challengeTask, appStats, true);
                        appStats.setGlobalScore(appStats.getGlobalScore() + challengeTask.getRewardPoints());

                        StorageManager.saveStats(appStats);
                        StorageManager.saveTasks(globalDatabase);
                        onUpdate.run();
                    }
                });
            });
            getChildren().add(completeBtn);
        }

        // --- Context Menu for Deletion & Editing ---
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit Challenge");
        editItem.setOnAction(e -> openChallengeConfigDialog(challengeTask, appStats, globalDatabase, onUpdate));

        MenuItem deleteItem = new MenuItem("Permanently Delete Challenge");
        deleteItem.setStyle("-fx-text-fill: #FF6666; -fx-font-weight: bold;");
        deleteItem.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to permanently delete '" + challengeTask.getTextContent() + "'?\n\nThis cannot be undone and rewards will not be revoked.", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Delete Challenge");
            TaskDialogs.styleDialog(confirm);
            confirm.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    globalDatabase.remove(challengeTask);
                    StorageManager.saveTasks(globalDatabase);
                    onUpdate.run();
                }
            });
        });
        contextMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);

        this.setOnContextMenuRequested(e -> {
            if (isTimeLocked || isCompleted) {
                editItem.setDisable(true);
                editItem.setText("Edit Challenge (Locked)");
            } else {
                editItem.setDisable(false);
                editItem.setText("Edit Challenge");
            }
            contextMenu.show(this, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private void openChallengeConfigDialog(TaskItem challengeTask, AppStats appStats, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Configure Challenge");
        TaskDialogs.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        TextField nameInput = new TextField(challengeTask.getTextContent());
        nameInput.setPromptText("Challenge Name");
        TextArea descInput = new TextArea(challengeTask.getPerkDescription() != null ? challengeTask.getPerkDescription() : "");
        descInput.setPromptText("Challenge Lore / Rules...");
        descInput.setPrefRowCount(3);
        content.getChildren().addAll(new Label("Challenge Name:"), nameInput, new Label("Description & Rules:"), descInput);

        // --- Deadline Config ---
        content.getChildren().add(new Separator());
        Label timeLabel = new Label("Challenge Timeline:");
        timeLabel.setStyle("-fx-text-fill: #E0E0E0; -fx-font-weight: bold;");
        content.getChildren().add(timeLabel);

        GridPane timeGrid = new GridPane();
        timeGrid.setHgap(15); timeGrid.setVgap(10);

        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        if (challengeTask.getDeadline() != null) datePicker.setValue(challengeTask.getDeadline().toLocalDate());

        TextField timePicker = new TextField();
        timePicker.setMaxWidth(Double.MAX_VALUE);
        timePicker.setPromptText("HH:mm (24h)");
        if (challengeTask.getDeadline() != null) timePicker.setText(challengeTask.getDeadline().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        timePicker.setDisable(datePicker.getValue() == null);
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> timePicker.setDisable(newVal == null));

        timeGrid.add(new Label("Deadline Date:"), 0, 0);
        timeGrid.add(datePicker, 1, 0);
        timeGrid.add(new Label("Exact Time:"), 0, 1);
        timeGrid.add(timePicker, 1, 1);
        content.getChildren().add(timeGrid);

        content.getChildren().add(new Separator());
        Label styleLabel = new Label("Appearance & Styling:");
        styleLabel.setStyle("-fx-text-fill: #569CD6; -fx-font-weight: bold;");
        content.getChildren().add(styleLabel);

        GridPane styleGrid = new GridPane();
        styleGrid.setHgap(15); styleGrid.setVgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        styleGrid.getColumnConstraints().addAll(col1, col2);

        ComboBox<String> iconBox = new ComboBox<>();
        iconBox.getItems().addAll(TaskDialogs.ICON_LIST);
        iconBox.setValue(challengeTask.getIconSymbol() != null ? challengeTask.getIconSymbol() : "None");
        iconBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(iconBox, Priority.ALWAYS);

        ColorPicker iconColorPicker = new ColorPicker(Color.web(challengeTask.getIconColor() != null ? challengeTask.getIconColor() : "#FFFFFF"));
        iconColorPicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(iconColorPicker, Priority.ALWAYS);

        ColorPicker bgColorPicker = new ColorPicker(Color.web(challengeTask.getColorHex() != null && !challengeTask.getColorHex().equals("transparent") ? challengeTask.getColorHex() : "#2D2D30"));
        bgColorPicker.setMaxWidth(Double.MAX_VALUE);

        ColorPicker outlinePicker = new ColorPicker(Color.web(challengeTask.getCustomOutlineColor() != null && !challengeTask.getCustomOutlineColor().equals("transparent") ? challengeTask.getCustomOutlineColor() : "#FF8C00"));
        outlinePicker.setMaxWidth(Double.MAX_VALUE);

        HBox iconRow = new HBox(10, iconBox, iconColorPicker);

        styleGrid.add(new Label("Icon & Color:"), 0, 0); styleGrid.add(iconRow, 1, 0);
        styleGrid.add(new Label("Background Color:"), 0, 1); styleGrid.add(bgColorPicker, 1, 1);
        styleGrid.add(new Label("Outline Color:"), 0, 2); styleGrid.add(outlinePicker, 1, 2);

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

        content.getChildren().add(new Separator());
        Label lootLabel = new Label("Challenge Loot (Rewards upon completion):");
        lootLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        content.getChildren().add(lootLabel);

        GridPane lootGrid = new GridPane();
        lootGrid.setHgap(10); lootGrid.setVgap(10);
        ColumnConstraints lcol1 = new ColumnConstraints();
        lcol1.setMinWidth(150);
        ColumnConstraints lcol2 = new ColumnConstraints(); lcol2.setHgrow(Priority.ALWAYS);
        ColumnConstraints lcol3 = new ColumnConstraints(); lcol3.setHgrow(Priority.ALWAYS);
        lootGrid.getColumnConstraints().addAll(lcol1, lcol2, lcol3);

        TextField globalPtsField = new TextField(String.valueOf(challengeTask.getRewardPoints()));
        globalPtsField.setMaxWidth(Double.MAX_VALUE);
        lootGrid.add(new Label("Global Points:"), 0, 0);
        lootGrid.add(globalPtsField, 1, 0, 2, 1);

        lootGrid.add(new Label("Stat"), 0, 1);
        lootGrid.add(new Label("+ XP Reward"), 1, 1);
        lootGrid.add(new Label("+ Max Cap"), 2, 1);

        int r = 2;
        Map<String, TextField> rewardFields = new HashMap<>();
        Map<String, TextField> capFields = new HashMap<>();

        for (CustomStat stat : appStats.getCustomStats()) {
            Label statNameLabel = new Label(stat.getName());
            statNameLabel.setStyle("-fx-text-fill: " + (stat.getTextColor() != null ? stat.getTextColor() : "white") + ";");
            lootGrid.add(statNameLabel, 0, r);

            TextField rF = new TextField(); rF.setMaxWidth(Double.MAX_VALUE);
            if (challengeTask.getStatRewards().containsKey(stat.getId())) rF.setText(String.valueOf(challengeTask.getStatRewards().get(stat.getId())));
            rewardFields.put(stat.getId(), rF);
            lootGrid.add(rF, 1, r);

            TextField cF = new TextField(); cF.setMaxWidth(Double.MAX_VALUE);
            if (challengeTask.getStatCapRewards().containsKey(stat.getId())) cF.setText(String.valueOf(challengeTask.getStatCapRewards().get(stat.getId())));
            capFields.put(stat.getId(), cF);
            lootGrid.add(cF, 2, r);
            r++;
        }
        content.getChildren().add(lootGrid);

        content.getChildren().add(new Separator());
        Label hookLabel = new Label("Unlock Requirements (Stats needed to attempt):");
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
            for (Map.Entry<String, Integer> req : challengeTask.getStatRequirements().entrySet()) {
                CustomStat s = appStats.getCustomStats().stream().filter(x -> x.getId().equals(req.getKey())).findFirst().orElse(null);
                if (s != null) {
                    HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                    Label l = new Label("• Requires " + req.getValue() + " " + s.getName()); l.setStyle("-fx-text-fill: #E0E0E0;");
                    Button removeBtn = new Button("❌");
                    removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #FF6666; -fx-cursor: hand;");
                    removeBtn.setOnAction(e -> { challengeTask.getStatRequirements().remove(req.getKey()); refreshReqs[0].run(); });
                    row.getChildren().addAll(l, removeBtn);
                    activeReqsBox.getChildren().add(row);
                }
            }
        };
        refreshReqs[0].run();

        addStatBtn.setOnAction(e -> {
            if (statBox.getValue() != null) {
                challengeTask.getStatRequirements().put(statBox.getValue().getId(), amountSpinner.getValue());
                refreshReqs[0].run();
            }
        });
        content.getChildren().add(activeReqsBox);

        content.getChildren().add(new Separator());
        Label depLabel = new Label("Hook Tasks / Challenges:");
        depLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        content.getChildren().add(depLabel);

        MenuButton dependenciesMenu = new MenuButton("Select Parent Requirements...");
        dependenciesMenu.getStyleClass().add("custom-menu-btn");
        dependenciesMenu.setMaxWidth(Double.MAX_VALUE);
        List<String> selectedDeps = new ArrayList<>(challengeTask.getDependsOnTaskIds());
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
            if (other.getId().equals(challengeTask.getId()) || other.isArchived()) continue;

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

        dependenciesMenu.getItems().removeIf(menuItem -> menuItem instanceof Menu && ((Menu) menuItem).getItems().isEmpty());
        if (!othersMenu.getItems().isEmpty()) dependenciesMenu.getItems().add(othersMenu);

        dependenciesMenu.setText("Hooked Requirements (" + depCount[0] + ")");
        if (dependenciesMenu.getItems().isEmpty()) {
            CustomMenuItem emptyItem = new CustomMenuItem(new Label("No other tasks available."));
            emptyItem.setDisable(true);
            dependenciesMenu.getItems().add(emptyItem);
        }

        content.getChildren().add(dependenciesMenu);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(550, 700);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);

        String scrollCss = ".scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: transparent; } " +
                ".scroll-bar:vertical .track, .scroll-bar:horizontal .track { -fx-background-color: #1E1E1E; -fx-border-color: transparent; } " +
                ".scroll-bar:vertical .thumb, .scroll-bar:horizontal .thumb { -fx-background-color: #555555; -fx-background-radius: 5; }";
        scrollPane.getStylesheets().add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(scrollCss.getBytes()));

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                challengeTask.setTextContent(nameInput.getText().trim());
                challengeTask.setPerkDescription(descInput.getText().trim());
                challengeTask.setIconSymbol(iconBox.getValue());
                challengeTask.setIconColor(toHexString(iconColorPicker.getValue()));
                challengeTask.setColorHex(toHexString(bgColorPicker.getValue()));
                challengeTask.setCustomOutlineColor(toHexString(outlinePicker.getValue()));
                challengeTask.setDependsOnTaskIds(selectedDeps);

                if (datePicker.getValue() != null) {
                    try {
                        LocalTime time = LocalTime.MIDNIGHT;
                        if (!timePicker.getText().trim().isEmpty()) time = LocalTime.parse(timePicker.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                        challengeTask.setDeadline(LocalDateTime.of(datePicker.getValue(), time));
                    } catch (Exception ex) { challengeTask.setDeadline(LocalDateTime.of(datePicker.getValue(), LocalTime.MIDNIGHT)); }
                } else {
                    challengeTask.setDeadline(null);
                }

                try { challengeTask.setRewardPoints(Math.max(0, Integer.parseInt(globalPtsField.getText().trim()))); } catch(Exception ignore){}

                Map<String, Integer> newRewards = new HashMap<>();
                Map<String, Integer> newCaps = new HashMap<>();

                for (CustomStat stat : appStats.getCustomStats()) {
                    try {
                        int rewardVal = Integer.parseInt(rewardFields.get(stat.getId()).getText().trim());
                        if (rewardVal > 0) newRewards.put(stat.getId(), rewardVal);
                    } catch(Exception ignore){}

                    try {
                        int capVal = Integer.parseInt(capFields.get(stat.getId()).getText().trim());
                        if (capVal > 0) newCaps.put(stat.getId(), capVal);
                    } catch(Exception ignore){}
                }
                challengeTask.setStatRewards(newRewards);
                challengeTask.setStatCapRewards(newCaps);

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