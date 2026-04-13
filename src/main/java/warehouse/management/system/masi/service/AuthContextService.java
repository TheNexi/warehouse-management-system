package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.AuthSession;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.AuthSessionRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthContextService {

    private final CookieService cookieService;
    private final AuthSessionRepository authSessionRepository;

    public Employee requireAuthenticated(HttpServletRequest request) {
        String token = cookieService.getTokenFromCookies(request.getCookies());

        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return authSessionRepository.findByTokenAndExpiresAtAfter(token, LocalDateTime.now())
                .map(AuthSession::getEmployee)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Session expired"));
    }

    public Employee requireAdministrator(HttpServletRequest request) {
        Employee employee = requireAuthenticated(request);

        if (employee.getRole() != EmployeeRole.ADMINISTRATOR) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Administrator access required");
        }

        return employee;
    }
}
