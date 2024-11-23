package polytech.system.components.device;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class Device {
    private final int id;

    private double processingEndTime;

    public boolean isFree(double time) {
        return time >= processingEndTime;
    }
}
