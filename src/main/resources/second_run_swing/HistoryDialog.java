package com.raeden.ors_to_do;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HistoryDialog extends JDialog {

    private Map<LocalDate, Double> historyLog;

    public HistoryDialog(JFrame parent, Map<LocalDate, Double> historyLog) {
        super(parent, "7-Day Daily History", true);
        this.historyLog = historyLog;

        setSize(400, 250);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int padding = 30;
                int barWidth = 30;
                int gap = (width - (2 * padding) - (7 * barWidth)) / 6;

                int x = padding;
                int maxBarHeight = height - (2 * padding) - 20;

                // Draw background line
                g2.setColor(new Color(60, 63, 65));
                g2.drawLine(padding, height - padding, width - padding, height - padding);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E");
                LocalDate renderDate = LocalDate.now().minusDays(7);

                for (int i = 0; i < 7; i++) {
                    renderDate = renderDate.plusDays(1);
                    double completion = historyLog.getOrDefault(renderDate, 0.0);

                    int barHeight = (int) (maxBarHeight * completion);
                    int y = height - padding - barHeight;

                    // Determine Bar Color
                    if (completion >= 1.0) {
                        g2.setColor(new Color(46, 204, 113));
                    } else if (completion > 0) {
                        g2.setColor(new Color(241, 196, 15));
                    } else {
                        g2.setColor(new Color(231, 76, 60));
                        barHeight = 5;
                        y = height - padding - barHeight;
                    }

                    g2.fill(new RoundRectangle2D.Double(x, y, barWidth, barHeight, 10, 10));

                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String dayStr = renderDate.format(formatter);
                    int strWidth = g2.getFontMetrics().stringWidth(dayStr);
                    g2.drawString(dayStr, x + (barWidth / 2) - (strWidth / 2), height - 10);

                    x += barWidth + gap;
                }
            }
        };

        add(chartPanel, BorderLayout.CENTER);
    }
}