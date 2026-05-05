package warehouse.management.system.masi.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockUpdateRequest {

    private Integer newStockLevel;
    private Integer changeBy;
}
