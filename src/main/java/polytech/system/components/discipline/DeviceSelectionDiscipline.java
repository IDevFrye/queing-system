package polytech.system.components.discipline;

import lombok.Data;
import polytech.system.components.bid.BidStatus;
import polytech.system.components.device.Device;
import polytech.system.components.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class DeviceSelectionDiscipline {

    private final List<Device> devices;

    private int pointer = 0;

    public Optional<Device> findFreeDevice(double time, Event event) {

        List<Device> devicesFromPointerToEnd = devices.subList(pointer, devices.size());
        List<Device> devicesFromStartToPointer = devices.subList(0, pointer);

        List<Device> devicesInCircularOrder = new ArrayList<>(devicesFromPointerToEnd);
        devicesInCircularOrder.addAll(devicesFromStartToPointer);

        Optional<Device> firstFreeDevice = devicesInCircularOrder.stream()
                .filter(device -> device.isFree(time))
                .findFirst();

        if (event.getBidStatus() == BidStatus.PLACED_IN_BUFFER) {
            firstFreeDevice.ifPresent(device -> pointer = devices.indexOf(device));
            incrementPointer();
        }
        return firstFreeDevice;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % devices.size();
    }
}
