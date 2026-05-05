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
import warehouse.management.system.masi.model.Delivery;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Product;
import warehouse.management.system.masi.model.enums.DeliveryStatus;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.DeliveryRepository;
import warehouse.management.system.masi.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = DeliveryService.class)
@ActiveProfiles("test")
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @MockitoBean
    private DeliveryRepository deliveryRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private WarehouseService warehouseService;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private OrderHistoryService orderHistoryService;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should return deliveries for authenticated user")
    void shouldReturnDeliveriesForAuthenticatedUser() {
        Employee employee = createEmployee();

        when(authContextService.requireAuthenticated(request)).thenReturn(employee);
        when(deliveryRepository.findAllByOrderByDeliveryDateDesc()).thenReturn(List.of(createPendingDelivery()));

        ResponseEntity<?> response = deliveryService.getDeliveries(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deliveryRepository).findAllByOrderByDeliveryDateDesc();
    }

    @Test
    @DisplayName("Should accept pending delivery")
    void shouldAcceptPendingDelivery() {
        Employee employee = createEmployee();
        Delivery delivery = createPendingDelivery();

        when(authContextService.requireAuthenticated(request)).thenReturn(employee);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        ResponseEntity<?> response = deliveryService.acceptDelivery(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DeliveryStatus.ACCEPTED, delivery.getStatus());
        assertEquals(25, delivery.getProduct().getAvailability());
        verify(productRepository).save(delivery.getProduct());
        verify(deliveryRepository).save(delivery);
        verify(warehouseService).increaseStock(20, "admin");
        verify(orderHistoryService).record("DELIVERY_ACCEPTED", "Accepted delivery 1", "admin");
    }

    @Test
    @DisplayName("Should throw exception when delivery not found")
    void shouldThrowExceptionWhenDeliveryNotFound() {
        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(deliveryRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> deliveryService.acceptDelivery(999L, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Delivery not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when delivery is not pending")
    void shouldThrowExceptionWhenDeliveryIsNotPending() {
        Delivery delivery = createPendingDelivery();
        delivery.setStatus(DeliveryStatus.ACCEPTED);

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        ApiException exception = assertThrows(ApiException.class,
                () -> deliveryService.acceptDelivery(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Only pending deliveries can be accepted", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when delivery quantity is invalid")
    void shouldThrowExceptionWhenDeliveryQuantityIsInvalid() {
        Delivery delivery = createPendingDelivery();
        delivery.setQuantity(0);

        when(authContextService.requireAuthenticated(request)).thenReturn(createEmployee());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        ApiException exception = assertThrows(ApiException.class,
                () -> deliveryService.acceptDelivery(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Delivery quantity must be positive", exception.getMessage());
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

    private Delivery createPendingDelivery() {
        Product product = Product.builder()
                .id(1L)
                .name("Tape")
                .price(new BigDecimal("7.50"))
                .description("packaging")
                .category("Packaging")
                .availability(5)
                .build();

        return Delivery.builder()
                .id(1L)
                .deliveryDate(LocalDate.now())
                .status(DeliveryStatus.PENDING)
                .deliveryAddress("Main Warehouse")
                .courierCompany("DHL")
                .product(product)
                .quantity(20)
                .build();
    }
}
