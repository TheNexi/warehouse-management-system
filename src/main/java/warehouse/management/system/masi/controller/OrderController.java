package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.service.OrderHistoryService;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping("/orders/history")
    public ResponseEntity<?> getOrderHistory(HttpServletRequest request) {
        return orderHistoryService.getHistory(request);
    }
}
