package polytech.system.components.event;

import java.util.concurrent.atomic.AtomicInteger;

public class EventIdGenerator {
    private static EventIdGenerator instance;

    private EventIdGenerator() {
    }

    public static EventIdGenerator getInstance() {
        if (instance == null) {
            instance = new EventIdGenerator();
        }

        return instance;
    }

    private final AtomicInteger generator = new AtomicInteger();

    public int getNextEventId() {
        return generator.incrementAndGet();
    }
}
