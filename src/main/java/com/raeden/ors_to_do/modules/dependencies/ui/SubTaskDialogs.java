package com.raeden.ors_to_do.modules.dependencies.ui;

import com.raeden.ors_to_do.dependencies.models.SubTask;
import com.raeden.ors_to_do.dependencies.models.TaskItem;
import com.raeden.ors_to_do.dependencies.storage.StorageManager;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

import static com.raeden.ors_to_do.modules.dependencies.ui.TaskDialogs.styleDialog;

public class SubTaskDialogs {

    public static void showAddSubTaskDialog(TaskItem task, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Sub-tasks");
        dialog.setHeaderText("Enter sub-tasks (one per line):");
        styleDialog(dialog);

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(5);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-control-inner-background: #2D2D30; -fx-text-fill: white; -fx-prompt-text-fill: #858585;");

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textArea.getText().trim().isEmpty()) {
                String[] lines = textArea.getText().split("\\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        task.getSubTasks().add(new SubTask(line.trim()));
                    }
                }
                task.setExpanded(true);
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showEditSubTaskDialog(SubTask subTask, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Sub-task");
        dialog.setHeaderText(null);
        styleDialog(dialog);

        TextField textField = new TextField(subTask.getTextContent());
        textField.setPrefWidth(300);
        textField.setStyle("-fx-background-color: #2D2D30; -fx-text-fill: white; -fx-border-color: #3E3E42; -fx-border-radius: 3;");

        dialog.getDialogPane().setContent(textField);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Platform.runLater(textField::requestFocus);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && !textField.getText().trim().isEmpty()) {
                subTask.setTextContent(textField.getText().trim());
                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }

    public static void showTextToTaskDialog(TaskItem sourceTask, List<TaskItem> globalDatabase, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Text to Task");
        dialog.setHeaderText("Write/Paste text to generate tasks and sub-tasks.(Use '-' for sub-tasks)");
        styleDialog(dialog);

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(10);
        textArea.setPrefWidth(400);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-control-inner-background: #2D2D30; -fx-text-fill: white; -fx-prompt-text-fill: #858585;");

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Platform.runLater(textArea::requestFocus);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK && !textArea.getText().trim().isEmpty()) {
                String[] lines = textArea.getText().split("\\r?\\n");
                TaskItem lastTask = null;

                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;

                    boolean isSubTask = line.startsWith("-") || line.startsWith("*") || line.startsWith(" ") || line.startsWith("\t");
                    String cleanText = line.replaceFirst("^[\\s\\-\\*]+", "").trim();

                    if (isSubTask && lastTask != null) {
                        lastTask.getSubTasks().add(new SubTask(cleanText));
                        lastTask.setExpanded(true);
                    } else {
                        lastTask = new TaskItem(cleanText, sourceTask.getPriority(), sourceTask.getSectionId());
                        lastTask.setIconSymbol(sourceTask.getIconSymbol());
                        lastTask.setIconColor(sourceTask.getIconColor());
                        lastTask.setPrefix(sourceTask.getPrefix());
                        lastTask.setPrefixColor(sourceTask.getPrefixColor());
                        lastTask.setTaskType(sourceTask.getTaskType());

                        globalDatabase.add(lastTask);
                    }
                }

                StorageManager.saveTasks(globalDatabase);
                onUpdate.run();
            }
        });
    }
}