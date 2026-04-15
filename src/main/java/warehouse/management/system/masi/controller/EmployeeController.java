package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.PaymentRequest;
import warehouse.management.system.masi.service.EmployeeService;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public ResponseEntity<?> getEmployees(HttpServletRequest request) {
        return employeeService.getEmployees(request);
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeRequest employeeRequest,
                                            HttpServletRequest request) {
        return employeeService.createEmployee(employeeRequest, request);
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                            @RequestBody EmployeeRequest employeeRequest,
                                            HttpServletRequest request) {
        return employeeService.updateEmployee(id, employeeRequest, request);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id,
                                            HttpServletRequest request) {
        return employeeService.deleteEmployee(id, request);
    }

    @PostMapping("/employees/{id}/pay")
    public ResponseEntity<?> payEmployee(@PathVariable Long id,
                                         @RequestBody PaymentRequest paymentRequest,
                                         HttpServletRequest request) {
        return employeeService.payEmployee(id, paymentRequest, request);
    }
}
