package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class AuthContextService {

    private final CookieService cookieService;
    private final JwtTokenService jwtTokenService;
    private final EmployeeRepository employeeRepository;

    public Employee requireAuthenticated(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookies(request.getCookies());

        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        if (!jwtTokenService.isTokenValid(token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String username = jwtTokenService.getUsername(token);

        return employeeRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public Employee requireAdministrator(HttpServletRequest request) {
        Employee employee = requireAuthenticated(request);

        if (employee.getRole() != EmployeeRole.ADMINISTRATOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Administrator access required");
        }

        return employee;
    }
}
