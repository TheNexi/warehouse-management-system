package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.request.StockUpdateRequest;
import warehouse.management.system.masi.service.WarehouseService;

@RestController
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/stock")
    public ResponseEntity<?> getStock(HttpServletRequest request) {
        return warehouseService.getStock(request);
    }

    @PostMapping("/stock/update")
    public ResponseEntity<?> updateStock(@RequestBody StockUpdateRequest stockRequest,
                                         HttpServletRequest request) {
        return warehouseService.updateStock(stockRequest, request);
    }
}
