package polytech.system.components.buffer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BufferCellStatus {
    FREE("Свободно"),
    HAS_BID("Занято заявкой");

    private final String description;
}
