package com.raeden.ors_to_do.modules;

import com.raeden.ors_to_do.dependencies.models.AppStats;
import com.raeden.ors_to_do.dependencies.models.CustomPriority;
import com.raeden.ors_to_do.dependencies.models.SectionConfig;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.modules.dependencies.ui.FilterSortHeader;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskCard;
import com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs;
import com.raeden.ors_to_do.modules.dependencies.ui.ZenModeOverlay;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class DynamicModule extends StackPane {

    private BorderPane mainContent;
    private ZenModeOverlay zenOverlay;
    private FilterSortHeader filterSortHeader;

    private boolean isZenMode = false;
    private VBox listContainer;

    private SectionConfig config;
    private List<TaskItem> globalDatabase;
    private AppStats appStats;
    private Runnable syncCallback;

    private TextField inputField;
    private TextField prefixField;
    private ComboBox<CustomPriority> priorityBox;
    private List<Timeline> activeTimelines = new ArrayList<>();

    public DynamicModule(SectionConfig config, List<TaskItem> globalDatabase, AppStats appStats, Runnable syncCallback) {
        this.config = config;
        this.globalDatabase = globalDatabase;
        this.appStats = appStats;
        this.syncCallback = syncCallback;

        mainContent = new BorderPane();
        mainContent.setPadding(new Insets(15));

        // --- NEW: Hide Zen Mode on Notes Pages ---
        Runnable zenToggleAction = config.isNotesPage() ? () -> {} : this::toggleZenMode;

        zenOverlay = new ZenModeOverlay(config, appStats, globalDatabase, zenToggleAction, syncCallback, activeTimelines, this::reorderTasks);
        filterSortHeader = new FilterSortHeader(config, appStats, globalDatabase, zenToggleAction, this::refreshList);

        if (config.isNotesPage()) {
            // Hide the Zen Mode button from the header entirely if it's a notes page
            filterSortHeader.getChildren().forEach(node -> {
                if (node instanceof HBox) {
                    ((HBox) node).getChildren().removeIf(n -> n instanceof Button && ((Button) n).getText().contains("Zen Mode"));
                }
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

        buildInputPanel();
        refreshList();
    }

    private void buildInputPanel() {
        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setPadding(new Insets(15, 0, 0, 0));

        // --- NEW: Hide Prefix on Notes Pages ---
        if (config.isShowPrefix() && !config.isNotesPage()) {
            prefixField = new TextField();
            prefixField.setPromptText("[PREFIX]");
            prefixField.setPrefWidth(80);
            prefixField.getStyleClass().add("input-field");
            inputPanel.getChildren().add(prefixField);
        }

        inputField = new TextField();

        // --- NEW: Custom Prompt for Notes Pages ---
        if (config.isNotesPage()) {
            inputField.setPromptText("Enter new note for " + config.getName() + "...");
        } else if (config.isRewardsPage()) {
            inputField.setPromptText("Enter new reward...");
        } else {
            inputField.setPromptText("Enter new task for " + config.getName() + "...");
        }

        inputField.getStyleClass().add("input-field");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputPanel.getChildren().add(inputField);

        // --- NEW: Hide Priority on Notes Pages ---
        if (config.isShowPriority() && !config.isNotesPage()) {
            priorityBox = new ComboBox<>();
            priorityBox.getItems().addAll(appStats.getCustomPriorities());
            if (!appStats.getCustomPriorities().isEmpty()) priorityBox.setValue(appStats.getCustomPriorities().get(1));
            TaskDialogs.setupPriorityBoxColors(priorityBox);
            inputPanel.getChildren().add(priorityBox);
        }

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");
        Button clearBtn = new Button("Clear");

        inputPanel.getChildren().addAll(addBtn, clearBtn);

        addBtn.setOnAction(e -> addTask());
        inputField.setOnAction(e -> addTask());
        clearBtn.setOnAction(e -> {
            inputField.clear();
            if (prefixField != null) prefixField.clear();
        });

        mainContent.setBottom(inputPanel);
    }

    private void toggleZenMode() {
        if (config.isNotesPage()) return; // Failsafe

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

    private int getPriorityWeight(CustomPriority p) {
        if (p == null) return -1;
        int idx = appStats.getCustomPriorities().indexOf(p);
        return idx == -1 ? 999 : idx;
    }

    public void refreshList() {
        for (Timeline t : activeTimelines) t.stop();
        activeTimelines.clear();

        if (isZenMode) {
            zenOverlay.refreshZenMode(false);
            return;
        }

        listContainer.getChildren().clear();
        int availableCount = 0;
        int completedCount = 0;

        Set<String> uniqueTags = new HashSet<>();
        List<TaskItem> tasksToDisplay = new ArrayList<>();

        for (TaskItem task : globalDatabase) {
            if (task.getSectionId() != null && task.getSectionId().equals(config.getId()) && !task.isArchived()) {

                String tag = null;
                if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) tag = task.getWorkType();
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

        // --- NEW: Force Pinned Items to the top on Notes Pages ---
        String sortMode = filterSortHeader.getSortMode();
        if (!sortMode.equals("Custom Order")) {
            tasksToDisplay.sort((t1, t2) -> {
                if (config.isNotesPage()) {
                    if (t1.isPinned() && !t2.isPinned()) return -1;
                    if (!t1.isPinned() && t2.isPinned()) return 1;
                }

                switch (sortMode) {
                    case "Oldest First": return t1.getDateCreated().compareTo(t2.getDateCreated());
                    case "Alphabetical": return t1.getTextContent().compareToIgnoreCase(t2.getTextContent());
                    case "Priority: Low to High": return Integer.compare(getPriorityWeight(t1.getPriority()), getPriorityWeight(t2.getPriority()));
                    case "Priority: High to Low": return Integer.compare(getPriorityWeight(t2.getPriority()), getPriorityWeight(t1.getPriority()));
                    case "Most Recent":
                    default: return t2.getDateCreated().compareTo(t1.getDateCreated());
                }
            });
        } else if (config.isNotesPage()) {
            // Even in custom order, force pins to the top
            tasksToDisplay.sort((t1, t2) -> {
                if (t1.isPinned() && !t2.isPinned()) return -1;
                if (!t1.isPinned() && t2.isPinned()) return 1;
                return 0; // Maintain existing custom order for non-pinned items
            });
        }

        filterSortHeader.updateBadges(availableCount, completedCount);

        if (tasksToDisplay.isEmpty()) {
            // --- NEW: Custom empty state text ---
            String emptyText = "Add a task to get started!";
            if (config.isNotesPage()) emptyText = "Add a note to your board!";
            else if (config.isRewardsPage()) emptyText = "Add a reward to your shop!";

            Label emptyLabel = new Label(emptyText);
            emptyLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            listContainer.getChildren().add(emptyLabel);
        } else {
            Runnable onUpdateTrigger = () -> {
                refreshList();
                if (syncCallback != null) syncCallback.run();
            };

            for (TaskItem task : tasksToDisplay) {
                listContainer.getChildren().add(new TaskCard(
                        task, config, appStats, globalDatabase, onUpdateTrigger, activeTimelines, this::reorderTasks
                ));
            }
        }

        if (config.isShowTags()) {
            filterSortHeader.updateFilterPills(uniqueTags, this::refreshList);
        }
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

    private void addTask() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        CustomPriority defaultPrio = null;
        if (config.isShowPriority() && !config.isNotesPage() && priorityBox != null) defaultPrio = priorityBox.getValue();
        else if (config.isShowPriority() && !config.isNotesPage() && !appStats.getCustomPriorities().isEmpty()) defaultPrio = appStats.getCustomPriorities().get(0);

        TaskItem newTask = new TaskItem(text, defaultPrio, config.getId());

        if (config.isShowPrefix() && !config.isNotesPage() && prefixField != null) {
            String pText = prefixField.getText().trim();
            if (!pText.isEmpty()) {
                if (!pText.startsWith("[")) pText = "[" + pText;
                if (!pText.endsWith("]")) pText = pText + "]";
                newTask.setPrefix(pText.toUpperCase());
                newTask.setPrefixColor("#4EC9B0");
            }
        }
        if (config.isShowWorkType() && !config.isNotesPage()) newTask.setWorkType("General");

        globalDatabase.add(newTask);

        if (filterSortHeader.getSortMode().equals("Most Recent")) refreshList();
        else { filterSortHeader.forceSortMode("Most Recent"); refreshList(); }

        inputField.clear();
        if (prefixField != null) prefixField.clear();
        StorageManager.saveTasks(globalDatabase);
    }
}