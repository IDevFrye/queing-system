package polytech.statistics;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StatisticsHolder {
    private static StatisticsHolder instance;

    private StatisticsHolder() {
    }

    public static StatisticsHolder getInstance() {
        if (instance == null) {
            instance = new StatisticsHolder();
        }

        return instance;
    }

    private Map<Integer, ProducerStatistics> producerStatistics;

    private Map<Integer, Double> deviceWorkTime = new HashMap<>();

    private Map<String, BidLifecycleTimings> bidLifecycleTimings = new HashMap<>();
}
