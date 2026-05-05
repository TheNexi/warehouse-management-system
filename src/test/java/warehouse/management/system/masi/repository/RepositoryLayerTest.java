package warehouse.management.system.masi.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import warehouse.management.system.masi.model.AuthSession;
import warehouse.management.system.masi.model.Delivery;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.OrderHistory;
import warehouse.management.system.masi.model.Payment;
import warehouse.management.system.masi.model.Product;
import warehouse.management.system.masi.model.Warehouse;
import warehouse.management.system.masi.model.enums.DeliveryStatus;
import warehouse.management.system.masi.model.enums.EmployeeRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
class RepositoryLayerTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Test
    @DisplayName("Should execute employee repository custom methods")
    void shouldExecuteEmployeeRepositoryCustomMethods() {
        Employee employee = employeeRepository.save(buildEmployee("john"));

        Optional<Employee> found = employeeRepository.findByUsername("john");
        boolean exists = employeeRepository.existsByUsername("john");

        assertTrue(found.isPresent());
        assertEquals(employee.getUsername(), found.get().getUsername());
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should execute auth session repository methods")
    void shouldExecuteAuthSessionRepositoryMethods() {
        Employee employee = employeeRepository.save(buildEmployee("admin"));
        AuthSession session = AuthSession.builder()
                .token("token-123")
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        authSessionRepository.save(session);

        Optional<AuthSession> found = authSessionRepository.findByTokenAndExpiresAtAfter(
                "token-123", LocalDateTime.now());

        assertTrue(found.isPresent());

        authSessionRepository.deleteByToken("token-123");

        Optional<AuthSession> afterDelete = authSessionRepository.findByTokenAndExpiresAtAfter(
                "token-123", LocalDateTime.now());
        assertFalse(afterDelete.isPresent());
    }

    @Test
    @DisplayName("Should return deliveries ordered by date desc")
    void shouldReturnDeliveriesOrderedByDateDesc() {
        Product product = productRepository.save(buildProduct());

        Delivery older = Delivery.builder()
                .deliveryDate(LocalDate.now().minusDays(1))
                .status(DeliveryStatus.PENDING)
                .deliveryAddress("A")
                .courierCompany("DHL")
                .product(product)
                .quantity(10)
                .build();

        Delivery newer = Delivery.builder()
                .deliveryDate(LocalDate.now())
                .status(DeliveryStatus.PENDING)
                .deliveryAddress("B")
                .courierCompany("FedEx")
                .product(product)
                .quantity(20)
                .build();

        deliveryRepository.save(older);
        deliveryRepository.save(newer);

        List<Delivery> sorted = deliveryRepository.findAllByOrderByDeliveryDateDesc();

        assertEquals(2, sorted.size());
        assertTrue(sorted.get(0).getDeliveryDate().isAfter(sorted.get(1).getDeliveryDate()));
    }

    @Test
    @DisplayName("Should return order history sorted by createdAt desc")
    void shouldReturnOrderHistorySortedByCreatedAtDesc() {
        OrderHistory first = OrderHistory.builder()
                .operationType("A")
                .details("a")
                .performedBy("system")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        OrderHistory second = OrderHistory.builder()
                .operationType("B")
                .details("b")
                .performedBy("system")
                .createdAt(LocalDateTime.now())
                .build();

        orderHistoryRepository.save(first);
        orderHistoryRepository.save(second);

        List<OrderHistory> sorted = orderHistoryRepository.findAllByOrderByCreatedAtDesc();

        assertEquals(2, sorted.size());
        assertEquals("B", sorted.get(0).getOperationType());
    }

    @Test
    @DisplayName("Should persist payment warehouse and product entities")
    void shouldPersistPaymentWarehouseAndProductEntities() {
        Employee employee = employeeRepository.save(buildEmployee("worker"));

        Payment payment = Payment.builder()
                .employee(employee)
                .amount(new BigDecimal("1000.00"))
                .bonusAmount(new BigDecimal("100.00"))
                .paymentDate(LocalDate.now())
                .build();

        Warehouse warehouse = Warehouse.builder()
                .address("Main Warehouse")
                .capacity(10000)
                .currentStockLevel(100)
                .build();

        paymentRepository.save(payment);
        warehouseRepository.save(warehouse);

        assertEquals(1, paymentRepository.count());
        assertEquals(1, warehouseRepository.count());
        assertEquals(1, productRepository.count());
    }

    private Employee buildEmployee(String username) {
        return Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .position("Warehouseman")
                .role(EmployeeRole.WAREHOUSEMAN)
                .username(username)
                .passwordHash("hash")
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .name("Tape")
                .price(new BigDecimal("7.50"))
                .description("desc")
                .category("Packaging")
                .availability(10)
                .build();
    }
}
