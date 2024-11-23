package polytech.gui.main;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelinePanel extends JPanel {
    private static final int ROW_HEIGHT = 30;
    private static final int TIME_MARKER_WIDTH = 2;
    private final Map<String, List<EventMarker>> eventMarkers;
    private List<String> labels;
    private int width;
    public TimelinePanel() {
        this.labels = new ArrayList<>();
        this.eventMarkers = new HashMap<>();
        this.width = 1072;
    }
    public void initialize(List<String> labels) {
        this.labels = labels;
        this.eventMarkers.clear();
        for (String label : labels) {
            eventMarkers.put(label, new ArrayList<>());
        }
        repaint();
    }

    public void addEvent(String label, int time, String details, String bidId) {
        if (!eventMarkers.containsKey(label)) {
            throw new IllegalArgumentException("Unknown label: " + label);
        }

        List<EventMarker> markers = eventMarkers.get(label);
        markers.add(new EventMarker(time * 15, details, bidId));
        this.width = Math.max(this.width, time * 15 + 100);

        if (label.startsWith("Прибор")) {
            String bufferLabel = "Буфер";
            if (eventMarkers.containsKey(bufferLabel)) {
                List<EventMarker> bufferMarkers = eventMarkers.get(bufferLabel);
                bufferMarkers.add(new EventMarker(time * 15, "Выход из буфера: " + bidId, bidId));
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 0;
        int pixelsPerSecond = 15;
        int timeStep = 10;
        int maxTime = width / pixelsPerSecond;

        for (String label : labels) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(0, y, width, y);

            g2d.setColor(Color.BLACK);
            g2d.drawString(label, 10, y + ROW_HEIGHT / 2);

            List<EventMarker> markers = eventMarkers.get(label);
            if (markers != null) {
                Map<String, Integer> lastPositions = new HashMap<>();
                for (EventMarker marker : markers) {
                    int x = marker.time;

                    g2d.setColor(Color.RED);
                    g2d.fillRect(x, y + ROW_HEIGHT / 4, TIME_MARKER_WIDTH, ROW_HEIGHT / 2);

                    boolean isSource = label.startsWith("И");
                    boolean isEntry = !lastPositions.containsKey(marker.bidId);
                    int textYOffset;

                    if (isSource) {
                        textYOffset = 10;
                    } else {
                        textYOffset = isEntry ? 10 : -10;
                    }

                    g2d.setColor(Color.BLACK);
                    g2d.drawString(marker.details, x + 5, y + ROW_HEIGHT / 2 + textYOffset);

                    if (lastPositions.containsKey(marker.bidId)) {
                        int previousX = lastPositions.get(marker.bidId);
                        g2d.setColor(Color.BLUE);
                        g2d.drawLine(previousX + TIME_MARKER_WIDTH / 2, y + ROW_HEIGHT / 2,
                                x + TIME_MARKER_WIDTH / 2, y + ROW_HEIGHT / 2);
                    }

                    lastPositions.put(marker.bidId, x);
                }
            }

            y += ROW_HEIGHT;
        }

        int timelineY = y + 20;
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, timelineY, width, timelineY);

        for (int currentTime = 0; currentTime <= maxTime; currentTime += timeStep) {
            int x = currentTime * pixelsPerSecond;
            g2d.drawLine(x, timelineY - 5, x, timelineY + 5);
            g2d.drawString(String.valueOf(currentTime), x + 2, timelineY + 20);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, labels.size() * ROW_HEIGHT + 40);
    }

    private static class EventMarker {
        int time;
        String details;
        String bidId;

        EventMarker(int time, String details, String bidId) {
            this.time = time;
            this.details = details;
            this.bidId = bidId;
        }
    }
}

