package polytech.system.components.producer;

import lombok.Builder;
import lombok.Data;
import polytech.system.components.bid.Bid;

@Data
@Builder(setterPrefix = "with")
public class Producer {
    private final int id;

    private int producedOrdersCount;

    public Bid generateBid() {
        return Bid.builder()
            .withName(String.format("%d-%d", id, ++producedOrdersCount))
            .withProducer(this)
            .build();
    }
}
