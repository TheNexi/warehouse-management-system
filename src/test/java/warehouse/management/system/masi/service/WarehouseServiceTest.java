package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Warehouse;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.WarehouseRepository;
import warehouse.management.system.masi.request.StockUpdateRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = WarehouseService.class)
@ActiveProfiles("test")
class WarehouseServiceTest {

    @Autowired
    private WarehouseService warehouseService;

    @MockitoBean
    private WarehouseRepository warehouseRepository;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private OrderHistoryService orderHistoryService;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should return existing warehouse stock")
    void shouldReturnExistingWarehouseStock() {
        Warehouse warehouse = createWarehouse();

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        ResponseEntity<?> response = warehouseService.getStock(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(warehouse, response.getBody());
    }

    @Test
    @DisplayName("Should create default warehouse when none exists")
    void shouldCreateDefaultWarehouseWhenNoneExists() {
        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(warehouseRepository.findAll()).thenReturn(List.of());
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = warehouseService.getStock(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    @DisplayName("Should update stock using absolute mode")
    void shouldUpdateStockUsingAbsoluteMode() {
        Warehouse warehouse = createWarehouse();
        StockUpdateRequest stockUpdateRequest = new StockUpdateRequest();
        stockUpdateRequest.setNewStockLevel(800);

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        ResponseEntity<?> response = warehouseService.updateStock(stockUpdateRequest, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(800, warehouse.getCurrentStockLevel());
        verify(orderHistoryService).record("STOCK_UPDATE", "Stock level set to 800", "admin");
    }

    @Test
    @DisplayName("Should throw exception when both stock modes are provided")
    void shouldThrowExceptionWhenBothStockModesAreProvided() {
        Warehouse warehouse = createWarehouse();
        StockUpdateRequest stockUpdateRequest = new StockUpdateRequest();
        stockUpdateRequest.setNewStockLevel(900);
        stockUpdateRequest.setChangeBy(5);

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        ApiException exception = assertThrows(ApiException.class,
                () -> warehouseService.updateStock(stockUpdateRequest, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Provide exactly one stock change mode", exception.getMessage());
    }

    @Test
    @DisplayName("Should increase stock")
    void shouldIncreaseStock() {
        Warehouse warehouse = createWarehouse();

        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        warehouseService.increaseStock(100, "admin");

        assertEquals(720, warehouse.getCurrentStockLevel());
        verify(orderHistoryService).record("STOCK_UPDATE", "Stock increased by 100", "admin");
    }

    @Test
    @DisplayName("Should throw exception when stock exceeds capacity")
    void shouldThrowExceptionWhenStockExceedsCapacity() {
        Warehouse warehouse = createWarehouse();
        warehouse.setCurrentStockLevel(11990);

        when(warehouseRepository.findAll()).thenReturn(List.of(warehouse));

        ApiException exception = assertThrows(ApiException.class,
                () -> warehouseService.increaseStock(20, "admin"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Stock level must be between 0 and capacity", exception.getMessage());
    }

    private Warehouse createWarehouse() {
        return Warehouse.builder()
                .id(1L)
                .address("Main Warehouse")
                .capacity(12000)
                .currentStockLevel(620)
                .build();
    }

    private Employee createEmployee() {
        return Employee.builder()
                .id(1L)
                .firstName("System")
                .lastName("Administrator")
                .position("Administrator")
                .role(EmployeeRole.ADMINISTRATOR)
                .username("admin")
                .passwordHash("hash")
                .build();
    }
}
