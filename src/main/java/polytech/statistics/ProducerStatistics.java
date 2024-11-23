package polytech.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "with")
public class ProducerStatistics {
    private int generatedBidsCount;
    private int rejectedBidsCount;
    private double totalBidsInSystemTime;
    private double totalBidsWaitingTime;
    private double squaredTotalBidsWaitingTime;
    private double totalBidsProcessingTime;
    private double squaredTotalBidsProcessingTime;
    public void incrementGeneratedBidsCount() {
        generatedBidsCount++;
    }
    public void incrementRejectedBidsCount() {
        rejectedBidsCount++;
    }
    public void addTotalBidsInSystemTime(double time) {
        totalBidsInSystemTime += time;
    }
    public void addTotalBidsWaitingTime(double time) {
        totalBidsWaitingTime += time;
        squaredTotalBidsWaitingTime += time * time;
    }
    public void addTotalBidsProcessingTime(double time) {
        totalBidsProcessingTime += time;
        squaredTotalBidsProcessingTime += time * time;
    }
}
