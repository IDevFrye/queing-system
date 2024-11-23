package polytech.system.components.bid;

import lombok.Builder;
import lombok.Data;
import polytech.system.components.producer.Producer;

@Data
@Builder(setterPrefix = "with")
public class Bid {
    private final String name;
    public static boolean inBuffer = false;

    private Producer producer;
    public void markAsInBuffer() {
        inBuffer = true;
    }
    public static boolean wasInBuffer() {
        return inBuffer;
    }
}
