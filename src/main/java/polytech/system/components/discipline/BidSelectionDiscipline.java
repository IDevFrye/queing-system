package polytech.system.components.discipline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import polytech.system.components.bid.Bid;
import polytech.system.components.buffer.Buffer;
import polytech.system.components.buffer.BufferPlacementAndRejectionDispatcher;

import java.util.Optional;

@Data
@Builder(setterPrefix = "with")
@AllArgsConstructor
public class BidSelectionDiscipline {
    private final Buffer buffer;

    private int pointer = 0;

    private int highestPriorityBidPackageNumber;

    public BidSelectionDiscipline(Buffer buffer) {
        this.buffer = buffer;
        highestPriorityBidPackageNumber = -1;
    }

    public Pair<Optional<Bid>, Integer> selectNextBid() {
        if (buffer.isEmpty()) {
            return new ImmutablePair<>(Optional.empty(), 0);
        }
        int bidToDevice = getNextBidIndex();
        incrementPointer();

        Bid bidToReturn = Bid.builder()
                .withName(buffer.getBids()[bidToDevice].getName())
                .withProducer(buffer.getBids()[bidToDevice].getProducer())
                .build();
        buffer.getBids()[bidToDevice] = null;
        buffer.decrementNumberOfBidsInBuffer();
        return new ImmutablePair<>(Optional.of(bidToReturn), bidToDevice);
    }

    private int getNextBidIndex() {
        int startBidIndex = pointer;
        for (int i = 0; i <= buffer.getCapacity(); i++) {
            if (buffer.getBids()[pointer] != null) {
                break;
            }
            incrementPointer();
        }
        return pointer;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % buffer.getBids().length;
    }


}
