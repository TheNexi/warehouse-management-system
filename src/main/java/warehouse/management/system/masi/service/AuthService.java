package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.repository.EmployeeRepository;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;
import warehouse.management.system.masi.response.LoginResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final JwtTokenService jwtTokenService;
    private final AuthContextService authContextService;
    private final EmployeeService employeeService;

    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response) {
        validateLoginRequest(request);
        Employee employee = getEmployeeByUsername(request.getUsername());
        validatePassword(request.getPassword(), employee);
        String token = jwtTokenService.generateToken(employee);
        cookieService.setTokenCookie(response, token);
        return ResponseEntity.ok(new LoginResponse(employee.getId(), employee.getUsername(), employee.getRole()));
    }

    public ResponseEntity<?> register(EmployeeRequest request, HttpServletRequest httpRequest) {
        return employeeService.createEmployee(request, httpRequest);
    }

    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        cookieService.clearTokenCookie(response);
        return ResponseEntity.ok(new ApiMessageResponse("Logged out"));
    }

    public ResponseEntity<?> isAuthorized(HttpServletRequest request) {
        authContextService.requireAuthenticated(request);
        return ResponseEntity.ok(new ApiMessageResponse("Authorized"));
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }
    }

    private Employee getEmployeeByUsername(String username) {
        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    private void validatePassword(String rawPassword, Employee employee) {
        String passwordHash = employee.getPasswordHash();

        try {
            if (!passwordEncoder.matches(rawPassword, passwordHash)) {
                if (passwordHash != null && rawPassword.equals(passwordHash)) {
                    // One-time migration of legacy plain passwords to Argon2 hashes.
                    employee.setPasswordHash(passwordEncoder.encode(rawPassword));
                    employeeRepository.save(employee);
                    return;
                }

                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }
        } catch (RuntimeException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }
}
