package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.models.CustomStat;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.ChallengeCard;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.PerkCard;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.StatCard;
import com.raeden.ors_to_do.modules.dependencies.ui.cards.TaskCard;
import com.raeden.ors_to_do.modules.dependencies.ui.components.DynamicInputPanel;
import com.raeden.ors_to_do.modules.dependencies.ui.components.FilterSortHeader;
import com.raeden.ors_to_do.modules.dependencies.ui.layout.ZenModeOverlay;
import com.raeden.ors_to_do.modules.dependencies.ui.menus.DynamicContextMenu;
import com.raeden.ors_to_do.modules.dependencies.ui.utils.DynamicSortHelper;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.*;

public class DynamicModule extends StackPane {

    private BorderPane mainContent;
    private ZenModeOverlay zenOverlay;
    private FilterSortHeader filterSortHeader;
    private VBox listContainer;

    private boolean isZenMode = false;
    private SectionConfig config;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable syncCallback;
    private List<Timeline> activeTimelines = new ArrayList<>();

    public DynamicModule(SectionConfig config, List<TaskItem> globalDatabase, AppStats appStats, Runnable syncCallback) {
        this.config = config;
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        this.syncCallback = syncCallback;

        mainContent = new BorderPane();
        mainContent.setPadding(new Insets(15));

        boolean isSpecialOverlay = config.isNotesPage() || config.isStatPage() || config.isPerkPage() || config.isChallengePage();
        Runnable zenToggleAction = isSpecialOverlay ? () -> {} : this::toggleZenMode;

        zenOverlay = new ZenModeOverlay(config, appStats, globalDatabase, zenToggleAction, syncCallback, activeTimelines, this::reorderTasks);
        filterSortHeader = new FilterSortHeader(config, appStats, globalDatabase, zenToggleAction, this::refreshList);

        if (isSpecialOverlay) {
            filterSortHeader.getChildren().forEach(node -> {
                if (node instanceof HBox) ((HBox) node).getChildren().removeIf(n -> n instanceof Button && ((Button) n).getText().contains("Zen Mode"));
            });
        }

        getChildren().addAll(mainContent, zenOverlay);
        mainContent.setTop(filterSortHeader);

        listContainer = new VBox(8);
        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1E1E1E;");
        scrollPane.setBorder(Border.EMPTY);
        mainContent.setCenter(scrollPane);

        ContextMenu bgMenu = DynamicContextMenu.build(config, appStats, globalDatabase, this::refreshList, syncCallback);

        if (!config.isStatPage() && !config.isPerkPage() && !config.isChallengePage()) {
            scrollPane.setOnContextMenuRequested(e -> {
                Node target = (Node) e.getTarget();
                boolean isTaskCard = false;
                while (target != null) {
                    if (target instanceof TaskCard) { isTaskCard = true; break; }
                    target = target.getParent();
                }
                if (!isTaskCard) bgMenu.show(scrollPane, e.getScreenX(), e.getScreenY());
            });
        }

        scrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { if (bgMenu.isShowing()) bgMenu.hide(); });

        if (!config.isStatPage()) {
            Runnable onInputRefresh = () -> {
                if (config.isChallengePage() && !globalDatabase.isEmpty()) {
                    TaskItem newest = globalDatabase.get(globalDatabase.size() - 1);
                    if (newest.getSectionId() != null && newest.getSectionId().equals(config.getId())) {
                        newest.setChallengeCard(true);
                        StorageManager.saveTasks(globalDatabase);
                    }
                }
                refreshList();
            };

            DynamicInputPanel inputPanel = new DynamicInputPanel(config, appStats, globalDatabase, filterSortHeader, onInputRefresh);
            mainContent.setBottom(inputPanel);
        }

        refreshList();
    }

    private void toggleZenMode() {
        if (config.isNotesPage() || config.isStatPage() || config.isPerkPage() || config.isChallengePage()) return;
        isZenMode = !isZenMode;

        if (getScene() != null && getScene().getRoot() instanceof BorderPane) {
            Node sidebar = ((BorderPane) getScene().getRoot()).getLeft();
            if (sidebar != null) {
                sidebar.setVisible(!isZenMode);
                sidebar.setManaged(!isZenMode);
            }
        }

        if (isZenMode) {
            mainContent.setVisible(false);
            zenOverlay.setVisible(true);
            zenOverlay.refreshZenMode(false);
        } else {
            mainContent.setVisible(true);
            zenOverlay.setVisible(false);
            refreshList();
        }
    }

    public void refreshList() {
        for (Timeline t : activeTimelines) t.stop();
        activeTimelines.clear();

        if (isZenMode) { zenOverlay.refreshZenMode(false); return; }

        listContainer.getChildren().clear();

        if (config.isStatPage()) { loadStatPage(); return; }
        if (config.isPerkPage()) { loadPerkPage(); return; }
        if (config.isChallengePage()) { loadChallengePage(); return; }

        int availableCount = 0; int completedCount = 0;
        Set<String> uniqueTags = new HashSet<>();
        List<TaskItem> tasksToDisplay = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
            if (task.getSectionId() != null && task.getSectionId().equals(config.getId()) && !task.isArchived()) {
                String tag = null;
                if (config.isShowTaskType() && task.getTaskType() != null && !task.getTaskType().isEmpty()) tag = task.getTaskType();
                else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) tag = task.getPrefix();
                if (tag != null) uniqueTags.add(tag);

                boolean passesFilter = filterSortHeader.getActiveFilter().equals("All") || (tag != null && tag.equals(filterSortHeader.getActiveFilter()));

                if (passesFilter) {
                    tasksToDisplay.add(task);
                    if (!task.isFinished()) availableCount++;
                    else completedCount++;
                }
            }
        }

        DynamicSortHelper.sortTasks(tasksToDisplay, filterSortHeader.getSortMode(), config, appStats);
        filterSortHeader.updateBadges(availableCount, completedCount);

        if (tasksToDisplay.isEmpty()) {
            String emptyText = config.isNotesPage() ? "Add a note to your board!" : (config.isRewardsPage() ? "Add a reward to your shop!" : "Add a task to get started!");
            Label emptyLabel = new Label(emptyText);
            emptyLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyLabel);
        } else {
            Runnable onUpdateTrigger = () -> { refreshList(); if (syncCallback != null) syncCallback.run(); };
            for (TaskItem task : tasksToDisplay) {
                listContainer.getChildren().add(new TaskCard(task, config, appStats, globalDatabase, onUpdateTrigger, activeTimelines, this::reorderTasks));
            }
        }

        if (config.isShowTags()) filterSortHeader.updateFilterPills(uniqueTags, this::refreshList);
    }

    private void loadStatPage() {
        if (!appStats.isGlobalStatsEnabled() || appStats.getCustomStats().isEmpty()) {
            Label emptyMsg = new Label("No custom stats available. Go to Settings to create them.");
            emptyMsg.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyMsg.setMaxWidth(Double.MAX_VALUE); emptyMsg.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyMsg);
        } else {
            for (CustomStat stat : appStats.getCustomStats()) {
                listContainer.getChildren().add(new StatCard(stat, appStats, () -> { refreshList(); if (syncCallback != null) syncCallback.run(); }));
            }
        }
        filterSortHeader.updateBadges(appStats.getCustomStats().size(), 0);
    }

    // --- FIXED: Added sorting and active count tracking for Perks ---
    private void loadPerkPage() {
        List<TaskItem> perks = new ArrayList<>();
        int totalCount = 0;
        int activeCount = 0;

        for (TaskItem task : globalDatabase) {
            if (task.getSectionId() != null && task.getSectionId().equals(config.getId()) && !task.isArchived()) {
                perks.add(task);
                totalCount++;
                if (task.getPerkLevel() > 0 || task.getPerkUnlockedDate() != null) {
                    activeCount++;
                }
            }
        }

        DynamicSortHelper.sortTasks(perks, filterSortHeader.getSortMode(), config, appStats);

        if (perks.isEmpty()) {
            Label emptyMsg = new Label("Type a perk name in the bar below and click 'Add' to create your first Skill Tree Perk!");
            emptyMsg.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyMsg.setMaxWidth(Double.MAX_VALUE); emptyMsg.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyMsg);
        } else {
            for (TaskItem perk : perks) {
                listContainer.getChildren().add(new PerkCard(perk, appStats, globalDatabase, () -> { refreshList(); if (syncCallback != null) syncCallback.run(); }));
            }
        }
        filterSortHeader.updateBadges(totalCount, activeCount);
    }

    private void loadChallengePage() {
        List<TaskItem> challenges = new ArrayList<>();
        int availableCount = 0;
        int completedCount = 0;

        for (TaskItem task : globalDatabase) {
            if (task.getSectionId() != null && task.getSectionId().equals(config.getId()) && !task.isArchived()) {
                challenges.add(task);
                if (task.isFinished()) completedCount++;
                else availableCount++;
            }
        }

        DynamicSortHelper.sortTasks(challenges, filterSortHeader.getSortMode(), config, appStats);

        if (challenges.isEmpty()) {
            Label emptyMsg = new Label("Type a challenge name below to create a new conquerable Challenge!");
            emptyMsg.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyMsg.setMaxWidth(Double.MAX_VALUE); emptyMsg.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyMsg);
        } else {
            for (TaskItem challenge : challenges) {
                listContainer.getChildren().add(new ChallengeCard(challenge, appStats, globalDatabase, () -> { refreshList(); if (syncCallback != null) syncCallback.run(); }));
            }
        }
        filterSortHeader.updateBadges(availableCount, completedCount);
    }

    private void reorderTasks(String draggedId, String targetId) {
        if (draggedId.equals(targetId)) return;
        TaskItem draggedTask = null, targetTask = null;
        for (TaskItem task : globalDatabase) {
            if (task.getId().equals(draggedId)) draggedTask = task;
            if (task.getId().equals(targetId)) targetTask = task;
        }
        if (draggedTask != null && targetTask != null) {
            int draggedIdx = globalDatabase.indexOf(draggedTask);
            int targetIdx = globalDatabase.indexOf(targetTask);
            globalDatabase.remove(draggedIdx);
            if (draggedIdx < targetIdx) targetIdx--;
            globalDatabase.add(targetIdx, draggedTask);
            StorageManager.saveTasks(globalDatabase);
            filterSortHeader.resetSortMode();
            refreshList();
        }
    }
}