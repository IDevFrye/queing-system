package polytech.system.components.buffer;

import lombok.Data;
import polytech.system.components.bid.Bid;

@Data
public class Buffer {
    private int capacity;

    private Bid[] bids;

    private int numberOfBidsInBuffer;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.bids = new Bid[capacity];
        this.numberOfBidsInBuffer = 0;
    }

    public void incrementNumberOfBidsInBuffer() {
        numberOfBidsInBuffer++;
    }

    public void decrementNumberOfBidsInBuffer() {
        numberOfBidsInBuffer--;
    }

    public boolean isEmpty() {
        return numberOfBidsInBuffer == 0;
    }
}
