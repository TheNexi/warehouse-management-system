package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.AuthSession;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.repository.AuthSessionRepository;
import warehouse.management.system.masi.repository.EmployeeRepository;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.response.ApiMessageResponse;
import warehouse.management.system.masi.response.LoginResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${auth.session.hours:12}")
    private int sessionHours;

    private final EmployeeRepository employeeRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final AuthContextService authContextService;

    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response) {
        validateLoginRequest(request);
        Employee employee = getEmployeeByUsername(request.getUsername());
        validatePassword(request.getPassword(), employee.getPasswordHash());
        String token = UUID.randomUUID().toString();
        saveSession(employee, token);
        cookieService.setTokenCookie(response, token);
        return ResponseEntity.ok(new LoginResponse(employee.getId(), employee.getUsername(), employee.getRole()));
    }

    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = cookieService.getTokenFromCookies(request.getCookies());
        deleteSession(token);
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

    private void validatePassword(String rawPassword, String passwordHash) {
        if (!passwordEncoder.matches(rawPassword, passwordHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    private void saveSession(Employee employee, String token) {
        AuthSession authSession = AuthSession.builder()
                .token(token)
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(sessionHours))
                .build();

        authSessionRepository.save(authSession);
    }

    private void deleteSession(String token) {
        if (token != null && !token.isBlank()) {
            authSessionRepository.deleteByToken(token);
        }
    }
}
