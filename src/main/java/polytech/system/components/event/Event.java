package polytech.system.components.event;

import lombok.Builder;
import lombok.Data;
import polytech.system.components.bid.Bid;
import polytech.system.components.bid.BidStatus;

@Data
@Builder(setterPrefix = "with")
public class Event {

    private final int id;

    private final Bid bid;

    private final BidStatus bidStatus;

    private final double time;

    private String externalDescription;

    private int bufferIndex;
    private int wasInBuffer;

    private int deviceNumber;

    private double endProcessingTime;
}
