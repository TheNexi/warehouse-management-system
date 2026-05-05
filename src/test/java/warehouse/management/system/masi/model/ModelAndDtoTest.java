package warehouse.management.system.masi.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import warehouse.management.system.masi.model.enums.DeliveryStatus;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.request.PaymentRequest;
import warehouse.management.system.masi.request.ProductRequest;
import warehouse.management.system.masi.request.StockUpdateRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;
import warehouse.management.system.masi.response.ErrorResponse;
import warehouse.management.system.masi.response.LoginResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ModelAndDtoTest {

    @Test
    @DisplayName("Should create and read model entities")
    void shouldCreateAndReadModelEntities() {
        Employee employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .position("Warehouseman")
                .role(EmployeeRole.WAREHOUSEMAN)
                .username("john")
                .passwordHash("hash")
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Tape")
                .price(new BigDecimal("7.50"))
                .description("desc")
                .category("Packaging")
                .availability(20)
                .build();

        Delivery delivery = Delivery.builder()
                .id(1L)
                .deliveryDate(LocalDate.now())
                .status(DeliveryStatus.PENDING)
                .deliveryAddress("Main")
                .courierCompany("DHL")
                .product(product)
                .quantity(10)
                .build();

        AuthSession authSession = AuthSession.builder()
                .id(1L)
                .token("token")
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .employee(employee)
                .amount(new BigDecimal("1000.00"))
                .bonusAmount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.now())
                .build();

        OrderHistory orderHistory = OrderHistory.builder()
                .id(1L)
                .operationType("PRODUCT_CREATE")
                .details("Created")
                .createdAt(LocalDateTime.now())
                .performedBy("admin")
                .build();

        Warehouse warehouse = Warehouse.builder()
                .id(1L)
                .address("Main Warehouse")
                .capacity(10000)
                .currentStockLevel(500)
                .build();

        assertEquals("john", employee.getUsername());
        assertEquals("Tape", product.getName());
        assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
        assertEquals(employee, authSession.getEmployee());
        assertEquals(new BigDecimal("1000.00"), payment.getAmount());
        assertEquals("PRODUCT_CREATE", orderHistory.getOperationType());
        assertEquals(10000, warehouse.getCapacity());
    }

    @Test
    @DisplayName("Should create request DTOs")
    void shouldCreateRequestDtos() {
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setFirstName("John");
        employeeRequest.setLastName("Doe");
        employeeRequest.setPosition("Warehouseman");
        employeeRequest.setRole(EmployeeRole.WAREHOUSEMAN);
        employeeRequest.setUsername("john");
        employeeRequest.setPassword("secret");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setBonusAmount(new BigDecimal("10.00"));

        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Tape");
        productRequest.setPrice(new BigDecimal("7.50"));
        productRequest.setDescription("desc");
        productRequest.setCategory("Packaging");
        productRequest.setAvailability(12);

        StockUpdateRequest stockUpdateRequest = new StockUpdateRequest();
        stockUpdateRequest.setNewStockLevel(10);
        stockUpdateRequest.setChangeBy(null);

        assertEquals("John", employeeRequest.getFirstName());
        assertEquals("admin", loginRequest.getUsername());
        assertEquals(new BigDecimal("100.00"), paymentRequest.getAmount());
        assertEquals("Packaging", productRequest.getCategory());
        assertEquals(10, stockUpdateRequest.getNewStockLevel());
    }

    @Test
    @DisplayName("Should create response records and enums")
    void shouldCreateResponseRecordsAndEnums() {
        ApiMessageResponse apiMessageResponse = new ApiMessageResponse("ok");
        ErrorResponse errorResponse = new ErrorResponse("error");
        LoginResponse loginResponse = new LoginResponse(1L, "admin", EmployeeRole.ADMINISTRATOR);

        assertEquals("ok", apiMessageResponse.message());
        assertEquals("error", errorResponse.message());
        assertEquals("admin", loginResponse.username());
        assertEquals(EmployeeRole.ADMINISTRATOR, loginResponse.role());
        assertNotNull(DeliveryStatus.valueOf("PENDING"));
        assertNotNull(EmployeeRole.valueOf("WAREHOUSEMAN"));
    }
}
