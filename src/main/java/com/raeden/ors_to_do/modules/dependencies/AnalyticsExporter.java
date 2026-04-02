package com.raeden.ors_to_do.modules.dependencies;

import com.raeden.ors_to_do.dependencies.AppStats;
import com.raeden.ors_to_do.dependencies.TaskItem;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsExporter {

    public static void exportSectionAnalytics(AppStats.SectionConfig config, List<TaskItem> globalDatabase) {
        try {
            File exportFile = new File(System.getProperty("user.home") + "/Desktop/" + config.getName().replaceAll(" ", "_") + "_Analytics.html");
            FileWriter writer = new FileWriter(exportFile);

            int totalTasks = 0;
            int completedTasks = 0;
            int totalTimeSeconds = 0;

            Map<String, Integer> categoryMap = new HashMap<>();

            for (TaskItem task : globalDatabase) {
                if (task.getSectionId() != null && task.getSectionId().equals(config.getId())) {
                    totalTasks++;
                    if (task.isFinished()) completedTasks++;
                    totalTimeSeconds += task.getTimeSpentSeconds();

                    String key = "Uncategorized";
                    if (config.isShowWorkType() && task.getWorkType() != null && !task.getWorkType().isEmpty()) key = task.getWorkType();
                    else if (config.isShowPrefix() && task.getPrefix() != null && !task.getPrefix().isEmpty()) key = task.getPrefix();
                    else if (config.isShowPriority() && task.getPriority() != null) key = task.getPriority().getName();

                    // If tracking time, chart is Time-Based. If not, chart is Volume-Based.
                    int valueToAdd = config.isTrackTime() ? task.getTimeSpentSeconds() / 60 : 1;
                    categoryMap.put(key, categoryMap.getOrDefault(key, 0) + valueToAdd);
                }
            }

            StringBuilder labels = new StringBuilder();
            StringBuilder data = new StringBuilder();
            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                labels.append("'").append(entry.getKey()).append("',");
                data.append(entry.getValue()).append(",");
            }

            double completionRate = totalTasks == 0 ? 0 : ((double) completedTasks / totalTasks) * 100;
            String chartLabel = config.isTrackTime() ? "Time Spent (Minutes)" : "Task Volume";

            String htmlContent = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s Analytics</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { background-color: #1E1E1E; color: #E0E0E0; font-family: 'Segoe UI', sans-serif; padding: 40px; }
                    .header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #3E3E42; padding-bottom: 20px; }
                    h1 { color: #569CD6; }
                    .stats-container { display: flex; justify-content: space-around; margin-bottom: 40px; }
                    .stat-box { background-color: #2D2D30; padding: 20px; border-radius: 10px; width: 30%%; text-align: center; border: 1px solid #3E3E42; }
                    .stat-box p { font-size: 32px; font-weight: bold; color: #4EC9B0; margin:0;}
                    .chart-container { background-color: #2D2D30; padding: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.3); border: 1px solid #3E3E42;}
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>%s Dashboard</h1>
                    <p style="color: #AAAAAA;">Generated on %s</p>
                </div>
                
                <div class="stats-container">
                    <div class="stat-box"><h3>Completion Rate</h3><p>%s%%</p></div>
                    <div class="stat-box"><h3>Total Tasks</h3><p>%d</p></div>
                    <div class="stat-box"><h3>Total Time Tracked</h3><p>%dh %dm</p></div>
                </div>

                <div class="chart-container">
                    <canvas id="mainChart" height="100"></canvas>
                </div>

                <script>
                    Chart.defaults.color = '#AAAAAA';
                    new Chart(document.getElementById('mainChart').getContext('2d'), {
                        type: 'bar',
                        data: {
                            labels: [%s],
                            datasets: [{
                                label: '%s',
                                data: [%s],
                                backgroundColor: ['#4EC9B0', '#569CD6', '#E06666', '#FF8C00', '#C586C0'],
                                borderRadius: 5
                            }]
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(
                    config.getName(), config.getName(), LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    String.format("%.1f", completionRate), totalTasks,
                    (totalTimeSeconds / 3600), ((totalTimeSeconds % 3600) / 60),
                    labels.toString(), chartLabel, data.toString()
            );

            writer.write(htmlContent);
            writer.flush();
            writer.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Analytics Exported to Desktop:\n" + exportFile.getName());
            alert.setHeaderText("Export Successful");
            alert.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}