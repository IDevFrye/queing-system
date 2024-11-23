package polytech.gui.statistics;

import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import polytech.gui.main.MassServiceSystemFrame;
import polytech.statistics.ProducerStatistics;
import polytech.statistics.StatisticsHolder;
import polytech.system.MassServiceSystemParams;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class TotalStatisticsFrame extends JFrame {
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#.#######");
    private static final int WIDTH_PX = 1920;
    private static final int HEIGHT_PX = 1080;
    private static final int GRAPH_W_PX = (int) (WIDTH_PX / 2);
    private static final int GRAPH_H_PX = (int) (HEIGHT_PX / 1.5);
    private static final int GRAPH_X_POS = (int)(WIDTH_PX / 2.5);
    private static final int GRAPH_Y_POS = (int)(HEIGHT_PX / 6);
    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    @Getter
    private static DefaultCategoryDataset dataset;
    private JFreeChart createChart(XYSeries dataset) {
        return ChartFactory.createXYLineChart(
                "",
                "время",
                "загрузка приборов",
                new XYSeriesCollection(dataset)
        );
    }
    public void addTableData(int value, String rowKey, String columnKey) {
        dataset.addValue(value, rowKey, columnKey);
        createChart(MassServiceSystemFrame.deviceBusyDataset);
    }
    private static final List<String> STATISTICS_TABLE_COLUMN_NAMES = List.of(
        "Номер источника",
        "Количество сгенерированных заявок",
        "Вероятность отказа",
        "Среднее время пребывания заявок",
        "Среднее время ожидания заявок",
        "Среднее время обслуживания заявок",
        "Дисперсия среднего времени ожидания",
        "Дисперсия среднего времени обслуживания"
    );
    private static final List<String> DEVICES_TABLE_COLUMN_NAMES = List.of(
        "Номер прибора",
        "Коэффициент использования"
    );
    private void placeAndConfigureChartPanel() {
        chartPanel.setPreferredSize(new Dimension(GRAPH_W_PX, GRAPH_H_PX));
        chartPanel.setBounds(200, 500, GRAPH_W_PX, GRAPH_H_PX);
        getContentPane().add(chartPanel);
    }
    public void updateChart(CategoryDataset dataset) {
        chart.getCategoryPlot().setDataset(dataset);
    }
    private final JTable producersStatisticsTable = new JTable(new Object[][]{}, STATISTICS_TABLE_COLUMN_NAMES.toArray());
    private final DefaultTableModel producersStatisticsTableModel = new DefaultTableModel(0, STATISTICS_TABLE_COLUMN_NAMES.size());
    private final JScrollPane producersStatisticsTableScrollPane = new JScrollPane(producersStatisticsTable);
    private final JTable devicesStatisticsTable = new JTable(new Object[][]{}, DEVICES_TABLE_COLUMN_NAMES.toArray());
    private final DefaultTableModel devicesStatisticsTableModel = new DefaultTableModel(0, DEVICES_TABLE_COLUMN_NAMES.size());
    private final JScrollPane devicesStatisticsTableScrollPane = new JScrollPane(devicesStatisticsTable);
    private final StatisticsHolder statistics = StatisticsHolder.getInstance();
    private final MassServiceSystemParams serviceSystemParams;
    public TotalStatisticsFrame(MassServiceSystemParams serviceSystemParams) {
        this.serviceSystemParams = serviceSystemParams;
        setTitle("Результаты работы Системы");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        placeAndConfigureStatisticsTable();
        placeAndConfigureDevicesTable();
        setSize(WIDTH_PX, HEIGHT_PX);
        setResizable(false);
        setVisible(true);

        chart = createChart(MassServiceSystemFrame.deviceBusyDataset);

        XYPlot plot = chart.getXYPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        domainAxis.setRange(0, 1440);
        rangeAxis.setRange(0, 120);

        chartPanel = new ChartPanel(chart);
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Montserrat", Font.PLAIN, 14));
        table.setRowHeight(table.getRowHeight() + 10);

        table.setSelectionBackground(new Color(255, 255, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(new Color(245, 245, 245));
        table.setGridColor(new Color(200, 200, 200));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Montserrat", Font.BOLD, 14));
        header.setBackground(new Color(0, 0, 0));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new CustomCellRenderer());
        }
    }

    private static class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (column > 0 && value instanceof Number) {
                double cellValue = ((Number) value).doubleValue();
                double max = -Double.MAX_VALUE;
                double min = Double.MAX_VALUE;

                for (int i = 0; i < table.getRowCount(); i++) {
                    Object cellData = table.getValueAt(i, column);
                    if (cellData instanceof Number) {
                        double num = ((Number) cellData).doubleValue();
                        if (num > max) max = num;
                        if (num < min) min = num;
                    }
                }

                if (cellValue == max && !isSelected) {
                    cell.setBackground(new Color(252, 226, 225));
                } else if (cellValue == min && !isSelected) {
                    cell.setBackground(new Color(237, 252, 225));
                } else if (!isSelected) {
                    cell.setBackground(Color.WHITE);
                }
            } else if (!isSelected) {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }
    }

    private void placeAndConfigureDeviceBusyChartPanel() {

        chartPanel.setBounds(910, GRAPH_Y_POS, GRAPH_W_PX, GRAPH_H_PX);
        getContentPane().add(chartPanel);
    }

    private double findAvgRejectionProbability(StatisticsHolder statistics) {
        double rejectionProbabilitySum = 0;
        for (var producerStats : statistics.getProducerStatistics().entrySet()) {
            rejectionProbabilitySum += (double) producerStats.getValue().getRejectedBidsCount() / producerStats.getValue().getGeneratedBidsCount();
        }

        return rejectionProbabilitySum;
    }

    private double findAvgDevicesWorkTimeCoefficient(StatisticsHolder statistics) {
        double avgDevicesWorkTimeCoefficient = 0;
        for (var deviceStats : statistics.getDeviceWorkTime().entrySet()) {
            avgDevicesWorkTimeCoefficient += deviceStats.getValue();
        }

        return avgDevicesWorkTimeCoefficient / (serviceSystemParams.getMaxSimulationTime() * statistics.getDeviceWorkTime().size());
    }

    private double findAvgClientWaitTime(StatisticsHolder statistics) {
        double avgClientWaitTime = 0;
        for (var producerStats : statistics.getProducerStatistics().entrySet()) {
            avgClientWaitTime += producerStats.getValue().getTotalBidsInSystemTime() / producerStats.getValue().getGeneratedBidsCount();
        }

        return avgClientWaitTime / serviceSystemParams.getProducerCount();
    }

    private void placeAndConfigureStatisticsTable() {
        producersStatisticsTableScrollPane.setVisible(true);
        producersStatisticsTableModel.setColumnIdentifiers(STATISTICS_TABLE_COLUMN_NAMES.toArray());
        producersStatisticsTable.setModel(producersStatisticsTableModel);
        styleTable(producersStatisticsTable);
        producersStatisticsTableScrollPane.setBounds(50, 20, WIDTH_PX - 100, 250);
        getContentPane().add(producersStatisticsTableScrollPane);

        NavigableMap<Integer, ProducerStatistics> producerStatistics = new TreeMap<>(Integer::compareTo);
        producerStatistics.putAll(statistics.getProducerStatistics());

        for (var producerStats : producerStatistics.entrySet()) {
            ProducerStatistics value = producerStats.getValue();
            producersStatisticsTableModel.addRow(List.of(
                    producerStats.getKey(),
                    value.getGeneratedBidsCount(),
                    (double) value.getRejectedBidsCount() / value.getGeneratedBidsCount(),
                    value.getTotalBidsInSystemTime() / value.getGeneratedBidsCount(),
                    value.getTotalBidsWaitingTime() / (value.getGeneratedBidsCount() - value.getRejectedBidsCount()),
                    value.getTotalBidsProcessingTime() / (value.getGeneratedBidsCount() - value.getRejectedBidsCount()),
                    getWaitingDispersion(value),
                    getProcessingDispersion(value)
            ).toArray());
        }
    }

    private void placeAndConfigureDevicesTable() {
        devicesStatisticsTable.setVisible(true);
        devicesStatisticsTableModel.setColumnIdentifiers(DEVICES_TABLE_COLUMN_NAMES.toArray());
        devicesStatisticsTable.setModel(devicesStatisticsTableModel);
        styleTable(devicesStatisticsTable);
        devicesStatisticsTableScrollPane.setBounds(50, 320, 810, 540);
        getContentPane().add(devicesStatisticsTableScrollPane);

        NavigableMap<Integer, Double> devicesStatistics = new TreeMap<>(Integer::compareTo);
        devicesStatistics.putAll(statistics.getDeviceWorkTime());

        for (var deviceStats : devicesStatistics.entrySet()) {
            devicesStatisticsTableModel.addRow(List.of(
                    deviceStats.getKey(),
                    deviceStats.getValue() / serviceSystemParams.getMaxSimulationTime()
            ).toArray());
        }
    }

    private double getWaitingDispersion(ProducerStatistics value) {
        return (value.getSquaredTotalBidsWaitingTime() / value.getGeneratedBidsCount() -
            Math.pow(value.getTotalBidsWaitingTime() / value.getGeneratedBidsCount(), 2));
    }

    private double getProcessingDispersion(ProducerStatistics value) {
        return (value.getSquaredTotalBidsProcessingTime() / value.getGeneratedBidsCount() -
            Math.pow(value.getTotalBidsProcessingTime() / value.getGeneratedBidsCount(), 2));
    }
}
