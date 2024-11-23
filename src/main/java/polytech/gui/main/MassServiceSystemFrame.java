package polytech.gui.main;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import polytech.gui.params.ParamsInputFrame;
import polytech.gui.statistics.TotalStatisticsFrame;
import polytech.system.MassServiceSystemController;
import polytech.system.MassServiceSystemParams;
import polytech.system.components.bid.Bid;
import polytech.system.components.bid.BidStatus;
import polytech.system.components.buffer.BufferCellStatus;
import polytech.system.components.event.Event;
import polytech.gui.main.TimelinePanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.table.*;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.Optional;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MassServiceSystemFrame extends JFrame {
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#.#####");
    private static final int WIDTH_PX = 1920;
    private static final int HEIGHT_PX = 1080;
    private static final int GRAPH_W_PX = 585;
    private static final int GRAPH_H_PX = 507;
    private final JFreeChart deviceBusyChart;
    private final ChartPanel deviceBusyChartPanel;
    public static XYSeries deviceBusyDataset = new XYSeries("device Busy Dataset");
    private JFreeChart createDeviceBusyChart(XYSeries dataset) {
        return ChartFactory.createXYLineChart(
                "",
                "время",
                "загрузка приборов",
                new XYSeriesCollection(dataset)
        );
    }
    public void addDeviceBusyTableData(double time, int value) {
        deviceBusyDataset.add(time, value);
        updateDeviceBusyChart(deviceBusyDataset);
    }
    private void placeAndConfigureDeviceBusyChartPanel() {
        deviceBusyChartPanel.setPreferredSize(new Dimension(GRAPH_W_PX, GRAPH_H_PX));
        deviceBusyChartPanel.setBounds(1283, 23, 585, 507);
        getContentPane().add(deviceBusyChartPanel);
    }
    public void updateDeviceBusyChart(XYSeries dataset) {
        deviceBusyChart.getXYPlot().setDataset(new XYSeriesCollection(dataset));
    }
    private static int deviceBusyCount = 0;
    private static final int EVENT_TABLE_WIDTH_PERCENT = 50;
    private static final List<String> EVENTS_TABLE_COLUMN_NAMES = List.of(
        "№",
        "Время",
        "Признак",
        "Прибор",
        "Отказы",
        "Заявки"
    );
    private static final List<String> REJECTED_TABLE_ITEM = List.of(
            "Отказ"
    );
    private static final List<String> COMPLETED_TABLE_ITEM = List.of(
            "Выполнено"
    );
    private static final List<String> BUFFER_TABLE_COLUMN_NAMES = List.of(
        "Индекс",
        "Статус",
        "Заявка"
    );
    private static final List<String> DEVICES_TABLE_COLUMN_NAMES = List.of(
        "Индекс",
        "Заявка на обслуживании",
        "Окончание обслуживания"
    );
    private static final List<String> PRODUCERS_TABLE_COLUMN_NAMES = List.of(
        "Индекс",
        "Номер заявки",
        "Время генерации"
    );
    private static int CURRENT_COMPLETED_EVENTS_NUMBER = 0;
    private static int CURRENT_REJECTED_EVENTS_NUMBER = 0;
    private final JTable eventsTable = new JTable(new Object[][]{}, EVENTS_TABLE_COLUMN_NAMES.toArray());
    public static final DefaultTableModel eventsTableModel = new DefaultTableModel(0, EVENTS_TABLE_COLUMN_NAMES.size());
    private final JScrollPane eventsTableScrollPane = new JScrollPane(eventsTable);
    private final JTable rejectedTable = new JTable(new Object[][]{}, REJECTED_TABLE_ITEM.toArray());
    private final DefaultTableModel rejectedTableModel = new DefaultTableModel(0, REJECTED_TABLE_ITEM.size());
    private final JScrollPane rejectTableScrollPane = new JScrollPane(rejectedTable);
    private final JTable completedTable = new JTable(new Object[][]{}, COMPLETED_TABLE_ITEM.toArray());
    private final DefaultTableModel completedTableModel = new DefaultTableModel(0, COMPLETED_TABLE_ITEM.size());
    private final JScrollPane completedTableScrollPane = new JScrollPane(completedTable);
    private final JTable bufferTable = new JTable(new Object[][]{}, BUFFER_TABLE_COLUMN_NAMES.toArray());
    private final DefaultTableModel bufferTableModel = new DefaultTableModel(0, BUFFER_TABLE_COLUMN_NAMES.size());
    private final JScrollPane bufferTableScrollPane = new JScrollPane(bufferTable);
    private final JTable devicesTable = new JTable(new Object[][]{}, DEVICES_TABLE_COLUMN_NAMES.toArray());
    private final DefaultTableModel devicesTableModel = new DefaultTableModel(0, DEVICES_TABLE_COLUMN_NAMES.size());
    private final JScrollPane devicesTableScrollPane = new JScrollPane(devicesTable);
    private final JTable producersTable = new JTable(new Object[][]{}, PRODUCERS_TABLE_COLUMN_NAMES.toArray());
    private final DefaultTableModel producersTableModel = new DefaultTableModel(0, PRODUCERS_TABLE_COLUMN_NAMES.size());
    private final JScrollPane producersTableScrollPane = new JScrollPane(producersTable);
    private final JButton nextStepButton = new JButton("Следующий шаг");
    private final JButton autoModeButton = new JButton("Автоматический режим");
    private final JButton showStatisticsButton = new JButton("Посмотреть статистику");
    private final MassServiceSystemController massServiceSystemController;

//--------------------------------------------------------------------------------------------------------
    private static TimelinePanel timelinePanel;
    private List<String> timelineLabels = new ArrayList<>();
    public void updateTimelineDisplay(polytech.system.components.event.Event event) {
        List<String> labels = determineLabelsForEvent(event);
        for (String label : labels) {
            timelinePanel.addEvent(
                    label,
                    (int) event.getTime(),
                    event.getBid().getName(),
                    event.getBid().getName()
            );
        }
    }
    private void initComponents() {
        setLayout(null);
        timelinePanel = new TimelinePanel();
        timelineLabels = generateLabels();
        timelinePanel.initialize(timelineLabels);
        timelinePanel.setBounds(0, 0, 1072, 472);

        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(796, 539, 1072, 488);
        getContentPane().add(scrollPane);

        timelinePanel.setVisible(true);
        revalidate();
        repaint();
    }
    private List<String> generateLabels() {
        List<String> labels = new ArrayList<>();
        int producerCount = massServiceSystemController.getServiceSystemParams().getProducerCount();
        int deviceCount = massServiceSystemController.getServiceSystemParams().getDeviceCount();
        int bufferCapacity = massServiceSystemController.getServiceSystemParams().getBufferCapacity();
        for (int i = 1; i <= producerCount; i++) {
            labels.add("И" + i);
        }
        for (int i = 1; i <= deviceCount; i++) {
            labels.add("П" + i);
        }
        for (int i = bufferCapacity; i >= 1; i--) {
            labels.add("Б" + i);
        }
        labels.add("Отказ");
        return labels;
    }
    private static List<String> determineLabelsForEvent(Event event) {
        List<String> labels = new ArrayList<>();
        switch (event.getBidStatus()) {
            case GENERATED -> {
                labels.add("И" + event.getBid().getProducer().getId());
            }
            case PLACED_IN_BUFFER -> {
                if (event.getBufferIndex() != -1) {
                    labels.add("Б" + (event.getBufferIndex() + 1));
                }
            }
            case ON_DEVICE -> {
                labels.add("П" + event.getDeviceNumber());
                if (event.getBufferIndex() != -1 ) {
                    if(event.getWasInBuffer() == 1)
                    {
                        labels.add("Б" + (event.getBufferIndex() + 1));
                    }
                }
            }
            case COMPLETED -> {
                labels.add("П" + event.getDeviceNumber());
            }
            case REJECTED -> {
                labels.add("Отказ");
            }
            default -> throw new IllegalStateException("Unexpected event type: " + event.getBidStatus());
        }

        return labels;
    }
// --------------------------------------------------------------------------------------------------------
    public MassServiceSystemFrame(MassServiceSystemParams serviceSystemParams) {
        massServiceSystemController = new MassServiceSystemController(serviceSystemParams, this);
        setTitle("Имитация работы СМО");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        placeAndConfigureTables();
        placeAndConfigureButtons();
        setSize(WIDTH_PX, HEIGHT_PX);
        setResizable(false);

        initComponents();

        initProducersTable(massServiceSystemController.getEvents());
        initEventsTable(massServiceSystemController.getEvents());
        initTimelineTable(massServiceSystemController.getEvents());

        setVisible(true);

        deviceBusyChart = createDeviceBusyChart(deviceBusyDataset);

        XYPlot plot = deviceBusyChart.getXYPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        domainAxis.setRange(0, 1440);
        rangeAxis.setRange(0, 120);

        deviceBusyChartPanel = new ChartPanel(deviceBusyChart);
        placeAndConfigureDeviceBusyChartPanel();
    }
    private void initProducersTable(NavigableSet<Event> preDefinitionValue) {
        for(Event current : preDefinitionValue) {
            redrawProducersTableOnGeneratedBidEvent(current);
        }
    }
    private void initEventsTable(NavigableSet<Event> preDefinitionValue) {
        for(Event current : preDefinitionValue) {
            updateStatistics(current);
            redrawEventTableOnGeneratedBidEvent(current);
        }
    }
    private void initTimelineTable(NavigableSet<Event> preDefinitionValue) {
        for(Event current : preDefinitionValue) {
            updateTimelineDisplay(current);
        }
    }
    public Map<Integer, Double> deviceOperationalTime = new HashMap<>();
    public Map<Integer, Integer> producerFailureCount = new HashMap<>();
    public static Map<Integer, Integer> producerRequestCount = new HashMap<>();
    private void updateStatistics(Event event) {
        int producerId;
        switch (event.getBidStatus()) {
            case GENERATED -> {
                producerId = event.getBid().getProducer().getId();
                producerRequestCount.put(producerId,
                        producerRequestCount.getOrDefault(producerId, 0) + 1);

            }
            case ON_DEVICE -> {
                int deviceNumber = event.getDeviceNumber();
                double duration = event.getEndProcessingTime() - event.getTime();
                deviceOperationalTime.put(deviceNumber,
                        deviceOperationalTime.getOrDefault(deviceNumber, 0.0) + duration);
            }
            case REJECTED -> {
                producerId = event.getBid().getProducer().getId();
                producerFailureCount.put(producerId,
                        producerFailureCount.getOrDefault(producerId, 0) + 1);
            }

        }
    }
    private void placeAndConfigureTables() {
        placeAndConfigureRejectedTable();
        placeAndConfigureCompletedTable();
        placeAndConfigureEventsTable();
        placeAndConfigureBufferTable();
        placeAndConfigureDevicesTable();
        placeAndConfigureProducersTable();
        styleTable(rejectedTable);
        styleTable(completedTable);
        styleTable(eventsTable);
        styleTable(bufferTable);
        styleTable(devicesTable);
        styleTable(producersTable);
    }

    private void styleTable(JTable table) {
        table.setFont(loadCustomFont("Montserrat-Regular.ttf", 14f));
        table.setRowHeight(table.getRowHeight() + 10);

        table.setSelectionBackground(new Color(255, 255, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(new Color(245, 245, 245));
        table.setGridColor(new Color(200, 200, 200));

        JTableHeader header = table.getTableHeader();
        header.setFont(loadCustomFont("Montserrat-Semibold.ttf", 14f));
        header.setBackground(new Color(0, 0, 0));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        if (table.getColumnCount() == 3) {
            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(100);
            columnModel.getColumn(1).setPreferredWidth(300);
            columnModel.getColumn(2).setPreferredWidth(300);
        }

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                table.setSelectionBackground(new Color(237, 237, 237));
                table.setRowSelectionInterval(row, row);
            }
        });
    }
    private void placeAndConfigureRejectedTable() {
        rejectTableScrollPane.setVisible(true);
        rejectedTableModel.setColumnIdentifiers(REJECTED_TABLE_ITEM.toArray());
        rejectedTable.setModel(rejectedTableModel);
        rejectedTable.setFont(new Font("Serif", Font.PLAIN, 12));
        rejectedTable.setRowHeight(rejectedTable.getRowHeight() + 10);
        rejectTableScrollPane.setBounds( 52, 809, 177, 50);
        getContentPane().add(rejectTableScrollPane);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < completedTable.getColumnCount(); i++) {
            rejectedTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        rejectedTableModel.addRow(List.of("").toArray());

    }
    private void placeAndConfigureCompletedTable() {
        completedTableScrollPane.setVisible(true);
        completedTableModel.setColumnIdentifiers(COMPLETED_TABLE_ITEM.toArray());
        completedTable.setModel(completedTableModel);
        completedTable.setFont(new Font("Serif", Font.PLAIN, 12));
        completedTable.setRowHeight(completedTable.getRowHeight() + 10);
        completedTableScrollPane.setBounds(274, 809, 177, 50);
        getContentPane().add(completedTableScrollPane);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < completedTable.getColumnCount(); i++) {
            completedTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        completedTableModel.addRow(List.of("").toArray());
    }
    private void placeAndConfigureEventsTable() {
        eventsTableScrollPane.setVisible(true);
        eventsTableModel.setColumnIdentifiers(EVENTS_TABLE_COLUMN_NAMES.toArray());
        eventsTable.setModel(eventsTableModel);
        eventsTable.setFont(new Font("Serif", Font.PLAIN, 12));
        eventsTable.setRowHeight(eventsTable.getRowHeight() + 10);
        eventsTableScrollPane.setBounds(796, 23,
                442, 507);
        getContentPane().add(eventsTableScrollPane);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < eventsTable.getColumnCount(); i++) {
            eventsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        for (int i = 0; i < massServiceSystemController.getServiceSystemParams().getProducerCount(); i++) {
            eventsTableModel.addRow(List.of("И" + (i+1), "", "1").toArray());
        }
        for (int i = 0; i < massServiceSystemController.getServiceSystemParams().getDeviceCount(); i++) {
            eventsTableModel.addRow(List.of("П" + (i+1), "0.0", "0").toArray());
        }
        eventsTableModel.addRow(List.of("END", massServiceSystemController.getServiceSystemParams().getMaxSimulationTime(), "1").toArray());
    }
    private void placeAndConfigureBufferTable() {
        bufferTableScrollPane.setVisible(true);
        bufferTableModel.setColumnIdentifiers(BUFFER_TABLE_COLUMN_NAMES.toArray());
        bufferTable.setModel(bufferTableModel);
        bufferTable.setFont(new Font("Serif", Font.PLAIN, 24));
        bufferTable.setRowHeight(bufferTable.getRowHeight() + 10);
        bufferTableScrollPane.setBounds(52, 539, 699, 249);
        getContentPane().add(bufferTableScrollPane);

        for (int i = 0; i < massServiceSystemController.getServiceSystemParams().getBufferCapacity(); i++) {
            bufferTableModel.addRow(List.of((i + 1),
                    BufferCellStatus.FREE.getDescription(), "").toArray());
        }
    }
    private void placeAndConfigureDevicesTable() {
        devicesTableScrollPane.setVisible(true);
        devicesTableModel.setColumnIdentifiers(DEVICES_TABLE_COLUMN_NAMES.toArray());
        devicesTable.setModel(devicesTableModel);
        devicesTable.setFont(new Font("Serif", Font.PLAIN, 24));
        devicesTable.setRowHeight(devicesTable.getRowHeight() + 10);
        devicesTableScrollPane.setBounds(52, 281, 699, 249);
        getContentPane().add(devicesTableScrollPane);

        for (int i = 0; i < massServiceSystemController.getServiceSystemParams().getDeviceCount(); i++) {
            devicesTableModel.addRow(List.of((i + 1), "", "").toArray());
        }
    }
    private void placeAndConfigureProducersTable() {
        producersTableScrollPane.setVisible(true);
        producersTableModel.setColumnIdentifiers(PRODUCERS_TABLE_COLUMN_NAMES.toArray());
        producersTable.setModel(producersTableModel);
        producersTable.setFont(new Font("Serif", Font.PLAIN, 24));
        producersTable.setRowHeight(producersTable.getRowHeight() + 10);
        producersTableScrollPane.setBounds(52, 23, 699, 249);
        getContentPane().add(producersTableScrollPane);

        for (int i = 0; i < massServiceSystemController.getServiceSystemParams().getProducerCount(); i++) {
            producersTableModel.addRow(List.of((i + 1), "", "").toArray());
        }
    }
    private void placeAndConfigureButtons() {
        styleButton(nextStepButton);
        nextStepButton.setBounds(52, 975, 177, 52);
        nextStepButton.addActionListener(this::nextStepButtonActionPerformed);
        add(nextStepButton);

        styleButton(autoModeButton);
        autoModeButton.setBounds(274, 975, 177, 52);
        autoModeButton.addActionListener(this::autoModeButtonActionPerformed);
        add(autoModeButton);

        styleButton(showStatisticsButton);
        showStatisticsButton.setBounds(496, 975, 177, 52);
        showStatisticsButton.addActionListener(this::showStatisticsButtonActionPerformed);
        add(showStatisticsButton);
    }
    private Font loadCustomFont(String fontFileName, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/" + fontFileName))
                    .deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }
    private void styleButton(JButton button) {
        button.setFont(loadCustomFont("Montserrat-SemiBold.ttf", 12f));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(new Color(0, 0, 0));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2, true));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 50, 50));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 0, 0));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(90, 90, 90));
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(50, 50, 50));
            }
        });
    }

    private boolean nextStepButtonActionPerformed(ActionEvent actionEvent) {
        Optional<Event> performedEvent = massServiceSystemController.performNextStep();
        if (performedEvent.isEmpty()) {
            return false;
        }

        Event event = performedEvent.get();
        updateStatistics(event);

        switch (event.getBidStatus()) {
            case GENERATED -> {
                updateStatistics(event);
                redrawProducersTableOnGeneratedBidEvent(event);
                redrawEventTableOnGeneratedBidEvent(event);
                updateTimelineDisplay(event);
            }
            case PLACED_IN_BUFFER -> {
                redrawBufferOnPlacedBidEvent(event);
                updateTimelineDisplay(event);
                int producerId = event.getBid().getProducer().getId();
            }
            case ON_DEVICE -> {
                redrawBufferOnDeviceBidEvent(event);
                redrawDevicesOnDeviceBidEvent(event);
                redrawEventsOnDeviceBidEvent(event);
                updateTimelineDisplay(event);
            }
            case REJECTED -> {
                redrawDevicesOnReject(event);
                redrawBufferOnRejectedBidEvent(event);
            }
            case COMPLETED -> {
                redrawDevicesOnCompletedBidEvent(event);
                redrawEventsOnCompletedBidEvent(event);
                updateTimelineDisplay(event);
            }
        }
        return true;
    }

    private int findRowWithMinTime() {
        int minRowIndex = -1;
        double minTime = Double.MAX_VALUE;

        for (int row = 0; row < eventsTableModel.getRowCount(); row++) {
            Object timeValue = eventsTableModel.getValueAt(row, 1);

            if (timeValue instanceof Double) {
                double time = (Double) timeValue;
                if (time > 0 && time < minTime) {
                    minTime = time;
                    minRowIndex = row;
                }
            }
        }

        return minRowIndex;
    }




    private void setCustomRendererForTimeColumn() {
        int minRow = findRowWithMinTime();

        eventsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value instanceof Double) {
                    double time = (Double) value;

                    if (time > 0) {
                        cell.setText(TIME_FORMAT.format(time));
                    } else {
                        cell.setText("");
                    }
                } else {
                    cell.setText("");
                }
                if (row == minRow) {
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                    cell.setForeground(Color.RED);
                } else {
                    cell.setFont(cell.getFont().deriveFont(Font.PLAIN));
                    cell.setForeground(Color.BLACK);
                }
                return cell;
            }
        });
    }
    private void redrawDevicesOnReject(Event event) {
        int producerId = event.getBid().getProducer().getId();
        int failureCount = producerFailureCount.getOrDefault(producerId, 0);
        eventsTableModel.setValueAt(failureCount, producerId - 1, 4);

        for (var cur : massServiceSystemController.getEvents()) {
            if (cur.getTime() > event.getTime() &&
                    cur.getDeviceNumber() == event.getDeviceNumber() &&
                    cur.getBidStatus() == BidStatus.GENERATED) {

                redrawProducersTableOnGeneratedBidEvent(cur);
                redrawEventTableOnGeneratedBidEvent(cur);
                updateTimelineDisplay(cur);
            }
        }
    }
    private void redrawBufferOnPlacedBidEvent(Event event) {
        bufferTableModel.removeRow(event.getBufferIndex());
        bufferTableModel.insertRow(event.getBufferIndex(), List.of(event.getBufferIndex() + 1,
            BufferCellStatus.HAS_BID.getDescription() + " " + event.getBid().getName(),
            event.getBid().getName(), "").toArray());

        for(var cur: massServiceSystemController.getEvents()) {
            if(cur.getTime() > event.getTime() && cur.getDeviceNumber() == event.getDeviceNumber() && cur.getBidStatus() == BidStatus.GENERATED) {
                redrawProducersTableOnGeneratedBidEvent(cur);
                redrawEventTableOnGeneratedBidEvent(cur);
                updateTimelineDisplay(cur);
            }
        }
    }
    private void redrawBufferOnDeviceBidEvent(Event event) {
        deviceBusyCount++;
        final int numberOfDevices = Integer.parseInt(
                ParamsInputFrame.paramsInputLabelsAndFields.get(3).getJTextField().getText()
        );
        addDeviceBusyTableData(event.getTime(),
                (int) (((double) deviceBusyCount / (double) numberOfDevices) * 100)
        );
        bufferTableModel.removeRow(event.getBufferIndex());
        bufferTableModel.insertRow(event.getBufferIndex(),
                List.of(event.getBufferIndex() + 1,
                        BufferCellStatus.FREE.getDescription(), "").toArray()
        );
    }
    private void redrawBufferOnRejectedBidEvent(Event event) {
        bufferTableModel.removeRow(event.getBufferIndex());
        rejectedTableModel.setValueAt(event.getBid().getName(), 0, 0 );
        bufferTableModel.insertRow(event.getBufferIndex(), List.of(event.getBufferIndex() + 1,
            BufferCellStatus.FREE.getDescription(), "").toArray());
        updateTimelineDisplay(event);
    }

    private void redrawDevicesOnDeviceBidEvent(Event event) {
        devicesTableModel.setValueAt(event.getBid().getName(), event.getDeviceNumber() - 1, 1);
        devicesTableModel.setValueAt(TIME_FORMAT.format(event.getEndProcessingTime()), event.getDeviceNumber() - 1, 2);
    }

    private void redrawEventsOnDeviceBidEvent(Event event) {
        int producerCount = massServiceSystemController.getServiceSystemParams().getProducerCount();
        int deviceIndex = event.getDeviceNumber() - 1 + producerCount;
        double endProcessingTime = event.getEndProcessingTime();
        eventsTableModel.setValueAt(endProcessingTime, deviceIndex, 1);
        eventsTableModel.setValueAt(1, deviceIndex, 2);
        double operationalTime = deviceOperationalTime.getOrDefault(event.getDeviceNumber(), 0.0);
        eventsTableModel.setValueAt(operationalTime, deviceIndex, 3);
        eventsTableModel.fireTableDataChanged();
        setCustomRendererForTimeColumn();
    }

    private void redrawDevicesOnCompletedBidEvent(Event event) {
        deviceBusyCount--;
        completedTableModel.setValueAt(event.getBid().getName(), 0, 0 );
        devicesTableModel.setValueAt("", event.getDeviceNumber() - 1, 1);
        devicesTableModel.setValueAt("", event.getDeviceNumber() - 1, 2);
    }

    private void redrawEventsOnCompletedBidEvent(Event event) {
        int producerCount = massServiceSystemController.getServiceSystemParams().getProducerCount();
        int deviceIndex = event.getDeviceNumber() - 1 + producerCount;
        eventsTableModel.setValueAt(0, deviceIndex, 2);
        eventsTableModel.setValueAt(0.0, deviceIndex, 1);
        double operationalTime = deviceOperationalTime.getOrDefault(event.getDeviceNumber(), 0.0);
        eventsTableModel.setValueAt(operationalTime, deviceIndex, 3);
        eventsTableModel.fireTableDataChanged();
        setCustomRendererForTimeColumn();
    }

    public void redrawProducersTableOnGeneratedBidEvent(Event event) {
        producersTableModel.setValueAt(event.getBid().getName(),
                event.getBid().getProducer().getId() - 1, 1);
        producersTableModel.setValueAt(TIME_FORMAT.format(event.getTime()),
                event.getBid().getProducer().getId() - 1, 2);
    }

    public void redrawEventTableOnGeneratedBidEvent(Event event) {
        int producerId = event.getBid().getProducer().getId();
        eventsTableModel.setValueAt(event.getTime(), producerId - 1, 1);
        eventsTableModel.setValueAt(producerRequestCount.get(producerId), producerId - 1, 5);
        updateTimelineDisplay(event);
        eventsTableModel.fireTableDataChanged();
        setCustomRendererForTimeColumn();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void autoModeButtonActionPerformed(ActionEvent actionEvent) {
        while (nextStepButtonActionPerformed(actionEvent)) {
        }
    }

    private void showStatisticsButtonActionPerformed(ActionEvent actionEvent) {
        dispose();
        new TotalStatisticsFrame(massServiceSystemController.getServiceSystemParams());
    }
}