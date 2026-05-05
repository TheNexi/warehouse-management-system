package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Payment;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.EmployeeRepository;
import warehouse.management.system.masi.repository.PaymentRepository;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.PaymentRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EmployeeService.class)
@ActiveProfiles("test")
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private OrderHistoryService orderHistoryService;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should return employees for administrator")
    void shouldReturnEmployeesForAdministrator() {
        Employee admin = createAdmin();

        when(authContextService.requireAdministrator(request)).thenReturn(admin);
        when(employeeRepository.findAll()).thenReturn(List.of(admin));

        ResponseEntity<?> response = employeeService.getEmployees(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("Should create employee successfully")
    void shouldCreateEmployeeSuccessfully() {
        Employee admin = createAdmin();
        EmployeeRequest requestDto = createEmployeeRequest();
        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);

        when(authContextService.requireAdministrator(request)).thenReturn(admin);
        when(employeeRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");

        ResponseEntity<?> response = employeeService.createEmployee(requestDto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(employeeRepository).save(employeeCaptor.capture());
        assertEquals("encoded-secret", employeeCaptor.getValue().getPasswordHash());
        verify(orderHistoryService).record("EMPLOYEE_CREATE", "Created employee john", "admin");
    }

    @Test
    @DisplayName("Should throw exception when creating employee without password")
    void shouldThrowExceptionWhenCreatingEmployeeWithoutPassword() {
        Employee admin = createAdmin();
        EmployeeRequest requestDto = createEmployeeRequest();
        requestDto.setPassword(" ");

        when(authContextService.requireAdministrator(request)).thenReturn(admin);

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeService.createEmployee(requestDto, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Password is required", exception.getMessage());
    }

    @Test
    @DisplayName("Should update employee and password")
    void shouldUpdateEmployeeAndPassword() {
        Employee admin = createAdmin();
        Employee employee = createEmployee();
        EmployeeRequest requestDto = createEmployeeRequest();

        when(authContextService.requireAdministrator(request)).thenReturn(admin);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(employeeRepository.findByUsername("john")).thenReturn(Optional.of(employee));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");

        ResponseEntity<?> response = employeeService.updateEmployee(2L, requestDto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("encoded-secret", employee.getPasswordHash());
        verify(employeeRepository).save(employee);
        verify(orderHistoryService).record("EMPLOYEE_UPDATE", "Updated employee john", "admin");
    }

    @Test
    @DisplayName("Should delete employee")
    void shouldDeleteEmployee() {
        Employee admin = createAdmin();
        Employee employee = createEmployee();

        when(authContextService.requireAdministrator(request)).thenReturn(admin);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = employeeService.deleteEmployee(2L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(employeeRepository).delete(employee);
        verify(orderHistoryService).record("EMPLOYEE_DELETE", "Deleted employee john", "admin");
    }

    @Test
    @DisplayName("Should create payment for employee")
    void shouldCreatePaymentForEmployee() {
        Employee admin = createAdmin();
        Employee employee = createEmployee();
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("4000.00"));
        paymentRequest.setBonusAmount(new BigDecimal("300.00"));
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        when(authContextService.requireAdministrator(request)).thenReturn(admin);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = employeeService.payEmployee(2L, paymentRequest, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(paymentRepository).save(paymentCaptor.capture());
        assertNotNull(paymentCaptor.getValue().getPaymentDate());
        verify(orderHistoryService).record("EMPLOYEE_PAYMENT", "Issued payment for john", "admin");
        assertInstanceOf(Payment.class, response.getBody());
    }

    @Test
    @DisplayName("Should throw exception when payment values are negative")
    void shouldThrowExceptionWhenPaymentValuesAreNegative() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal("-1.00"));
        paymentRequest.setBonusAmount(BigDecimal.ZERO);

        when(authContextService.requireAdministrator(request)).thenReturn(createAdmin());
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(createEmployee()));

        ApiException exception = assertThrows(ApiException.class,
                () -> employeeService.payEmployee(2L, paymentRequest, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Amount and bonus cannot be negative", exception.getMessage());
    }

    private Employee createAdmin() {
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

    private Employee createEmployee() {
        return Employee.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .position("Warehouseman")
                .role(EmployeeRole.WAREHOUSEMAN)
                .username("john")
                .passwordHash("old")
                .build();
    }

    private EmployeeRequest createEmployeeRequest() {
        EmployeeRequest request = new EmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPosition("Warehouseman");
        request.setRole(EmployeeRole.WAREHOUSEMAN);
        request.setUsername("john");
        request.setPassword("secret123");
        return request;
    }
}
