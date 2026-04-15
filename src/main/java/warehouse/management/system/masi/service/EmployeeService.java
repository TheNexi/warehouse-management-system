package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.Payment;
import warehouse.management.system.masi.repository.EmployeeRepository;
import warehouse.management.system.masi.repository.PaymentRepository;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.PaymentRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthContextService authContextService;
    private final OrderHistoryService orderHistoryService;

    public ResponseEntity<?> getEmployees(HttpServletRequest request) {
        authContextService.requireAdministrator(request);
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    public ResponseEntity<?> createEmployee(EmployeeRequest request, HttpServletRequest httpRequest) {
        Employee admin = authContextService.requireAdministrator(httpRequest);
        validateCreateRequest(request);
        ensureUsernameAvailable(request.getUsername(), null);
        Employee employee = buildEmployee(request);
        employeeRepository.save(employee);
        orderHistoryService.record("EMPLOYEE_CREATE", "Created employee " + employee.getUsername(), admin.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    public ResponseEntity<?> updateEmployee(Long id, EmployeeRequest request, HttpServletRequest httpRequest) {
        Employee admin = authContextService.requireAdministrator(httpRequest);
        validateUpdateRequest(request);
        Employee employee = getEmployeeById(id);
        ensureUsernameAvailable(request.getUsername(), id);
        applyEmployeeData(employee, request);
        employeeRepository.save(employee);
        orderHistoryService.record("EMPLOYEE_UPDATE", "Updated employee " + employee.getUsername(), admin.getUsername());
        return ResponseEntity.ok(employee);
    }

    public ResponseEntity<?> deleteEmployee(Long id, HttpServletRequest request) {
        Employee admin = authContextService.requireAdministrator(request);
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
        orderHistoryService.record("EMPLOYEE_DELETE", "Deleted employee " + employee.getUsername(), admin.getUsername());
        return ResponseEntity.ok(new ApiMessageResponse("Employee deleted"));
    }

    public ResponseEntity<?> payEmployee(Long id, PaymentRequest request, HttpServletRequest httpRequest) {
        Employee admin = authContextService.requireAdministrator(httpRequest);
        Employee employee = getEmployeeById(id);
        validatePaymentRequest(request);
        Payment payment = buildPayment(employee, request);
        paymentRepository.save(payment);
        orderHistoryService.record("EMPLOYEE_PAYMENT", "Issued payment for " + employee.getUsername(), admin.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    private Employee buildEmployee(EmployeeRequest request) {
        return Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .role(request.getRole())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
    }

    private Payment buildPayment(Employee employee, PaymentRequest request) {
        return Payment.builder()
                .employee(employee)
                .amount(request.getAmount())
                .bonusAmount(request.getBonusAmount())
                .paymentDate(LocalDate.now())
                .build();
    }

    private Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void applyEmployeeData(Employee employee, EmployeeRequest request) {
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPosition(request.getPosition());
        employee.setRole(request.getRole());
        employee.setUsername(request.getUsername());

        if (!isBlank(request.getPassword())) {
            employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
    }

    private void ensureUsernameAvailable(String username, Long currentEmployeeId) {
        employeeRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(currentEmployeeId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Username already exists");
                });
    }

    private void validateCreateRequest(EmployeeRequest request) {
        validateCommonEmployeeFields(request);

        if (isBlank(request.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required");
        }
    }

    private void validateUpdateRequest(EmployeeRequest request) {
        validateCommonEmployeeFields(request);
    }

    private void validateCommonEmployeeFields(EmployeeRequest request) {
        if (request == null || request.getRole() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Employee request is invalid");
        }

        if (isBlank(request.getFirstName()) || isBlank(request.getLastName()) || isBlank(request.getPosition())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "First name, last name and position are required");
        }

        if (isBlank(request.getUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username is required");
        }
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null || request.getAmount() == null || request.getBonusAmount() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount and bonus are required");
        }

        if (request.getAmount().signum() < 0 || request.getBonusAmount().signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount and bonus cannot be negative");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
