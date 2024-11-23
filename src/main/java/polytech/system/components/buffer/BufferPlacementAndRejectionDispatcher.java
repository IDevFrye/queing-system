package polytech.system.components.buffer;

import lombok.Data;
import lombok.Getter;
import polytech.statistics.StatisticsHolder;
import polytech.system.components.bid.Bid;
import polytech.system.components.bid.BidStatus;
import polytech.system.components.event.Event;
import polytech.system.components.event.EventIdGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
public class BufferPlacementAndRejectionDispatcher {

    private final Buffer buffer;

    private int lastAddedPointer = 0;

    private final StatisticsHolder statistics = StatisticsHolder.getInstance();

    private final EventIdGenerator eventIdGenerator = EventIdGenerator.getInstance();

    public List<Event> placeBidInBuffer(Bid bid, double bidGenerationTime) {
        List<Event> placementResultEvents = new ArrayList<>();
        bid.markAsInBuffer();
        if (buffer.getNumberOfBidsInBuffer() == buffer.getCapacity()) {
            Bid bidToReject = Bid.builder()
                .withName(buffer.getBids()[lastAddedPointer].getName())
                .withProducer(buffer.getBids()[lastAddedPointer].getProducer())
                .build();

            buffer.getBids()[lastAddedPointer] = null;

            buffer.decrementNumberOfBidsInBuffer();
            placementResultEvents.add(Event.builder()
                .withId(eventIdGenerator.getNextEventId())
                .withBid(bidToReject)
                .withBidStatus(BidStatus.REJECTED)
                .withBufferIndex(lastAddedPointer)
                .withTime(bidGenerationTime)
                .build());

            statistics.getProducerStatistics().get(bidToReject.getProducer().getId()).incrementRejectedBidsCount();
            statistics.getBidLifecycleTimings().remove(bidToReject.getName());
        }
        int index = addBidToBuffer(bid);
        lastAddedPointer = index;

        placementResultEvents.add(Event.builder()
            .withId(eventIdGenerator.getNextEventId())
            .withBid(bid)
            .withBidStatus(BidStatus.PLACED_IN_BUFFER)
            .withTime(bidGenerationTime)
            .withBufferIndex(index)
            .build());
        return placementResultEvents;
    }

    private int addBidToBuffer(Bid bid) {
        int i = 0;
        for (i = 0; i < buffer.getCapacity(); i++) {
            if (buffer.getBids()[i] == null) {
                break;
            }
        }
        buffer.getBids()[i] = bid;
        buffer.incrementNumberOfBidsInBuffer();
        return i;
    }
}
