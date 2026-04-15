package warehouse.management.system.masi.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    private BigDecimal amount;
    private BigDecimal bonusAmount;
}
