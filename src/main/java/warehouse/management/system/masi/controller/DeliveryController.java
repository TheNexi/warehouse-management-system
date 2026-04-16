package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.service.DeliveryService;

@RestController
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/deliveries")
    public ResponseEntity<?> getDeliveries(HttpServletRequest request) {
        return deliveryService.getDeliveries(request);
    }

    @PostMapping("/deliveries/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id,
                                            HttpServletRequest request) {
        return deliveryService.acceptDelivery(id, request);
    }
}
