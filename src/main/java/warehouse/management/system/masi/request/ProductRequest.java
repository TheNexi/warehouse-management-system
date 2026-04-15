package warehouse.management.system.masi.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    private String name;
    private BigDecimal price;
    private String description;
    private String category;
    private Integer availability;
}
