package polytech.system.components.bid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BidStatus {
    GENERATED("Сгенерирована"),
    PLACED_IN_BUFFER("Установлена в буфер"),
    REJECTED("Отказ в обслуживании"),
    ON_DEVICE("Передана на прибор"),
    COMPLETED("Исполнена");

    private final String description;
}
