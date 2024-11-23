package polytech.system;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import polytech.gui.main.MassServiceSystemFrame;
import polytech.statistics.BidLifecycleTimings;
import polytech.statistics.ProducerStatistics;
import polytech.statistics.StatisticsHolder;
import polytech.system.components.bid.Bid;
import polytech.system.components.bid.BidStatus;
import polytech.system.components.buffer.Buffer;
import polytech.system.components.buffer.BufferPlacementAndRejectionDispatcher;
import polytech.system.components.device.Device;
import polytech.system.components.discipline.BidSelectionDiscipline;
import polytech.system.components.discipline.DeviceSelectionDiscipline;
import polytech.system.components.event.Event;
import polytech.system.components.event.EventIdGenerator;
import polytech.system.components.producer.Producer;
import polytech.system.time.distribution.PoissonDistributionLaw;
import polytech.system.time.distribution.UniformDistributionLaw;
import polytech.gui.main.TimelinePanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@RequiredArgsConstructor
public class MassServiceSystemController {
    private final MassServiceSystemFrame massServiceSystemFrame;
    private final List<Producer> producers = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private final DeviceSelectionDiscipline deviceSelectionDiscipline;
    private final Buffer buffer;
    private final BufferPlacementAndRejectionDispatcher bufferPlacementAndRejectionDispatcher;
    private final BidSelectionDiscipline bidSelectionDiscipline;
    private final NavigableSet<Event> events = new TreeSet<>(Comparator
        .comparingDouble(Event::getTime)
        .thenComparing(Event::getId));
    private final EventIdGenerator eventIdGenerator = EventIdGenerator.getInstance();
    private final PoissonDistributionLaw poissonDistributionLaw = new PoissonDistributionLaw();
    private final UniformDistributionLaw uniformDistributionLaw = new UniformDistributionLaw();
    private final MassServiceSystemParams serviceSystemParams;
    private final StatisticsHolder statistics = StatisticsHolder.getInstance();

    public MassServiceSystemController(MassServiceSystemParams serviceSystemParams, MassServiceSystemFrame massServiceSystemFrame) {
        this.massServiceSystemFrame = massServiceSystemFrame;
        initializeProducers(serviceSystemParams.getProducerCount());
        initializeDevices(serviceSystemParams.getDeviceCount());
        this.statistics.setProducerStatistics(producers.stream().collect(Collectors.toMap(Producer::getId, a -> new ProducerStatistics())));
        this.statistics.setDeviceWorkTime(devices.stream().collect(Collectors.toMap(Device::getId, a -> 0.0)));

        this.serviceSystemParams = serviceSystemParams;
        initializeFirstBids();
        this.buffer = new Buffer(serviceSystemParams.getBufferCapacity());
        this.bidSelectionDiscipline = new BidSelectionDiscipline(buffer);
        this.deviceSelectionDiscipline = new DeviceSelectionDiscipline(devices);
        this.bufferPlacementAndRejectionDispatcher = new BufferPlacementAndRejectionDispatcher(buffer);
    }
    private void initializeProducers(int producerCount) {
        if (producerCount < 1) {
            throw new IllegalArgumentException("Количество источников не может быть меньше 1");
        }

        IntStream.rangeClosed(1, producerCount).forEach(id -> producers.add(Producer.builder()
            .withId(id)
            .build()));
    }
    private void initializeDevices(int deviceCount) {
        if (deviceCount < 1) {
            throw new IllegalArgumentException("Количество приборов не может быть меньше 1");
        }

        IntStream.rangeClosed(1, deviceCount).forEach(id -> devices.add(Device.builder()
            .withId(id)
            .withProcessingEndTime(0.0)
            .build()));
    }
    private void initializeFirstBids() {
        AtomicReference<Double> initialTime = new AtomicReference<>(0.0);

        producers.forEach(producer -> {
            initialTime.set(initialTime.get() + uniformDistributionLaw
                    .getTime(serviceSystemParams.getMinDeviceProcessingTime(),
                            serviceSystemParams.getMaxDeviceProcessingTime()));

            Event generationEvent = Event.builder()
                .withId(eventIdGenerator.getNextEventId())
                .withBid(producer.generateBid())
                .withBidStatus(BidStatus.GENERATED)
                .withTime(initialTime.get())
                .withExternalDescription(BidStatus.GENERATED.getDescription() + " источником И" + producer.getId())
                .build();

            events.add(generationEvent);
            statistics.getBidLifecycleTimings().put(generationEvent.getBid().getName(), BidLifecycleTimings.builder()
                .withGenerationTime(initialTime.get())
                .build());
            statistics.getProducerStatistics().get(producer.getId()).incrementGeneratedBidsCount();
        });
    }
    public Optional<Event> performNextStep() {
        Event currentEvent;

        do {
            if (events.isEmpty()) {
                return Optional.empty();
            }
            currentEvent = events.first();
            events.remove(currentEvent);

            handleEvent(currentEvent);
        } while (currentEvent.getBidStatus() == BidStatus.GENERATED);
        return Optional.of(currentEvent);
    }
    private Event handleEvent(Event currentEvent) {
        switch (currentEvent.getBidStatus()) {
            case GENERATED -> {
                return placeBidToBufferAndGenerateNewBid(currentEvent);
            }
            case PLACED_IN_BUFFER, COMPLETED -> {
                return selectBidFromBufferAndSendToDevice(currentEvent);
            }
            case ON_DEVICE -> {
                return currentEvent;
            }
        }
        return null;
    }

    private Event placeBidToBufferAndGenerateNewBid(Event event) {
        double newEventTime = event.getTime() + uniformDistributionLaw
                .getTime(serviceSystemParams.getMinDeviceProcessingTime(),
                        serviceSystemParams.getMaxDeviceProcessingTime());

        if (newEventTime < serviceSystemParams.getMaxSimulationTime()) {
            Event generationEvent = Event.builder()
                    .withId(eventIdGenerator.getNextEventId())
                    .withBid(event.getBid().getProducer().generateBid())
                    .withBidStatus(BidStatus.GENERATED)
                    .withTime(newEventTime)
                    .withExternalDescription(BidStatus.GENERATED.getDescription()
                            + " источником И" + event.getBid().getProducer().getId())
                    .build();

            events.add(generationEvent);
            int producerId = event.getBid().getProducer().getId();

            MassServiceSystemFrame.producerRequestCount.put(producerId,
                    MassServiceSystemFrame.producerRequestCount.getOrDefault(producerId, 0) + 1);
            MassServiceSystemFrame.eventsTableModel.setValueAt(MassServiceSystemFrame.producerRequestCount.get(producerId), producerId - 1, 5);
            massServiceSystemFrame.redrawProducersTableOnGeneratedBidEvent(generationEvent);
            massServiceSystemFrame.redrawEventTableOnGeneratedBidEvent(generationEvent);
            massServiceSystemFrame.updateTimelineDisplay(event);

            statistics.getProducerStatistics().get(event.getBid().getProducer().getId()).incrementGeneratedBidsCount();
            statistics.getBidLifecycleTimings().put(generationEvent.getBid().getName(), BidLifecycleTimings.builder()
                    .withGenerationTime(newEventTime)
                    .build());

            Optional<Device> optionalDevice = deviceSelectionDiscipline.findFreeDevice(event.getTime(), event);
            if (optionalDevice.isPresent()) {
                double processingEndTime = event.getTime() + uniformDistributionLaw
                        .getTime(serviceSystemParams.getMinDeviceProcessingTime(), serviceSystemParams.getMaxDeviceProcessingTime());
                Device device = optionalDevice.get();
                device.setProcessingEndTime(processingEndTime);
                Bid bid = event.getBid();
                Event onDeviceEvent = Event.builder()
                        .withId(eventIdGenerator.getNextEventId())
                        .withBid(bid)
                        .withBidStatus(BidStatus.ON_DEVICE)
                        .withTime(event.getTime())
                        .withExternalDescription(BidStatus.ON_DEVICE.getDescription() + " П" + device.getId())
                        .withDeviceNumber(device.getId())
                        .withEndProcessingTime(processingEndTime)
                        .build();

                Event completedEvent = Event.builder()
                        .withId(eventIdGenerator.getNextEventId())
                        .withBid(bid)
                        .withBidStatus(BidStatus.COMPLETED)
                        .withDeviceNumber(device.getId())
                        .withExternalDescription(BidStatus.COMPLETED.getDescription() + " прибором П" + device.getId())
                        .withTime(processingEndTime)
                        .build();

                events.addAll(List.of(onDeviceEvent, completedEvent));

                BidLifecycleTimings timings = statistics.getBidLifecycleTimings().get(bid.getName());
                if (timings == null) {
                } else {
                    statistics.getProducerStatistics().get(bid.getProducer().getId())
                            .addTotalBidsInSystemTime(processingEndTime - timings.getGenerationTime());
                    statistics.getProducerStatistics().get(bid.getProducer().getId())
                            .addTotalBidsWaitingTime(event.getTime() - timings.getGenerationTime());
                    statistics.getProducerStatistics().get(bid.getProducer().getId())
                            .addTotalBidsProcessingTime(processingEndTime - event.getTime());
                    statistics.getDeviceWorkTime().put(device.getId(),
                            statistics.getDeviceWorkTime().get(device.getId()) + processingEndTime - event.getTime());
                    statistics.getBidLifecycleTimings().remove(bid.getName());
                }

            } else {
                events.addAll(bufferPlacementAndRejectionDispatcher.placeBidInBuffer(event.getBid(), event.getTime()));
            }
        }
        return event;
    }

    private Event selectBidFromBufferAndSendToDevice(Event event) {
        Optional<Device> optionalDevice = deviceSelectionDiscipline.findFreeDevice(event.getTime(), event);
        if (optionalDevice.isEmpty()) {
            return event;
        }

        Device device = optionalDevice.get();
        Pair<Optional<Bid>, Integer> bidToProcess = bidSelectionDiscipline.selectNextBid();
        if (bidToProcess.getLeft().isEmpty()) {
            return event;
        }

        Bid bid = bidToProcess.getLeft().get();
        int bufferIndex = bidToProcess.getRight();
        double processingEndTime = event.getTime() + uniformDistributionLaw
                .getTime(serviceSystemParams.getMinDeviceProcessingTime(), serviceSystemParams.getMaxDeviceProcessingTime());
        device.setProcessingEndTime(processingEndTime);
        Event onDeviceEvent = Event.builder()
                .withId(eventIdGenerator.getNextEventId())
                .withBid(bid)
                .withBidStatus(BidStatus.ON_DEVICE)
                .withWasInBuffer(1)
                .withTime(event.getTime())
                .withExternalDescription(BidStatus.ON_DEVICE.getDescription() + " П" + device.getId())
                .withBufferIndex(bufferIndex)
                .withDeviceNumber(device.getId())
                .withEndProcessingTime(processingEndTime)
                .build();

        Event completedEvent = Event.builder()
                .withId(eventIdGenerator.getNextEventId())
                .withBid(bid)
                .withBidStatus(BidStatus.COMPLETED)
                .withDeviceNumber(device.getId())
                .withExternalDescription(BidStatus.COMPLETED.getDescription() + " прибором П" + device.getId())
                .withTime(processingEndTime)
                .build();

        events.addAll(List.of(onDeviceEvent, completedEvent));
        BidLifecycleTimings timings = statistics.getBidLifecycleTimings().get(bid.getName());
        if (timings == null) {
        } else {
            statistics.getProducerStatistics().get(bid.getProducer().getId())
                    .addTotalBidsInSystemTime(processingEndTime - timings.getGenerationTime());
            statistics.getProducerStatistics().get(bid.getProducer().getId())
                    .addTotalBidsWaitingTime(event.getTime() - timings.getGenerationTime());
            statistics.getProducerStatistics().get(bid.getProducer().getId())
                    .addTotalBidsProcessingTime(processingEndTime - event.getTime());
            statistics.getDeviceWorkTime().put(device.getId(),
                    statistics.getDeviceWorkTime().get(device.getId()) + processingEndTime - event.getTime());
            statistics.getBidLifecycleTimings().remove(bid.getName());
        }
        return event;
    }
}
