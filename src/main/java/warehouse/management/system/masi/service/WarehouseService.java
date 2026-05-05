package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Warehouse;
import warehouse.management.system.masi.repository.WarehouseRepository;
import warehouse.management.system.masi.request.StockUpdateRequest;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AuthContextService authContextService;
    private final OrderHistoryService orderHistoryService;

    public ResponseEntity<?> getStock(HttpServletRequest request) {
        authContextService.requireAuthenticated(request);
        return ResponseEntity.ok(getOrCreateWarehouse());
    }

    public ResponseEntity<?> updateStock(StockUpdateRequest request, HttpServletRequest httpRequest) {
        Employee employee = authContextService.requireAuthenticated(httpRequest);
        Warehouse warehouse = getOrCreateWarehouse();
        int newLevel = resolveNewStockLevel(warehouse.getCurrentStockLevel(), request);
        validateStockLevel(newLevel, warehouse.getCapacity());
        warehouse.setCurrentStockLevel(newLevel);
        warehouseRepository.save(warehouse);
        orderHistoryService.record("STOCK_UPDATE", "Stock level set to " + newLevel, employee.getUsername());
        return ResponseEntity.ok(warehouse);
    }

    public void increaseStock(int quantity, String performedBy) {
        Warehouse warehouse = getOrCreateWarehouse();
        int newLevel = warehouse.getCurrentStockLevel() + quantity;
        validateStockLevel(newLevel, warehouse.getCapacity());
        warehouse.setCurrentStockLevel(newLevel);
        warehouseRepository.save(warehouse);
        orderHistoryService.record("STOCK_UPDATE", "Stock increased by " + quantity, performedBy);
    }

    private int resolveNewStockLevel(int currentStock, StockUpdateRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock request is required");
        }

        boolean hasAbsoluteValue = request.getNewStockLevel() != null;
        boolean hasRelativeValue = request.getChangeBy() != null;

        if (hasAbsoluteValue == hasRelativeValue) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Provide exactly one stock change mode");
        }

        return hasAbsoluteValue ? request.getNewStockLevel() : currentStock + request.getChangeBy();
    }

    private void validateStockLevel(int stockLevel, int capacity) {
        if (stockLevel < 0 || stockLevel > capacity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Stock level must be between 0 and capacity");
        }
    }

    private Warehouse getOrCreateWarehouse() {
        return warehouseRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefaultWarehouse);
    }

    private Warehouse createDefaultWarehouse() {
        Warehouse warehouse = Warehouse.builder()
                .address("Main Warehouse")
                .capacity(10000)
                .currentStockLevel(0)
                .build();

        return warehouseRepository.save(warehouse);
    }
}
