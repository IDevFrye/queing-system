package polytech.system;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class MassServiceSystemParams {
    private final int producerCount;

    private final int bufferCapacity;

    private final int deviceCount;

    private final double lambda;

    private final double maxSimulationTime;

    private final double minDeviceProcessingTime;

    private final double maxDeviceProcessingTime;
}
