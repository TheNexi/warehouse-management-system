package warehouse.management.system.masi.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import warehouse.management.system.masi.exception.ApiException;
import warehouse.management.system.masi.model.AuthSession;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.AuthSessionRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthContextService.class)
@ActiveProfiles("test")
class AuthContextServiceTest {

    @Autowired
    private AuthContextService authContextService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private AuthSessionRepository authSessionRepository;

    @Test
    @DisplayName("Should return authenticated employee")
    void shouldReturnAuthenticatedEmployee() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Employee employee = buildEmployee(EmployeeRole.ADMINISTRATOR);
        AuthSession session = AuthSession.builder()
                .id(1L)
                .token("token-1")
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", "token-1")});
        when(cookieService.getTokenFromCookies(any())).thenReturn("token-1");
        when(authSessionRepository.findByTokenAndExpiresAtAfter(eq("token-1"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        Employee result = authContextService.requireAuthenticated(request);

        assertEquals("admin", result.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when token is missing")
    void shouldThrowExceptionWhenTokenIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(null);
        when(cookieService.getTokenFromCookies(null)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> authContextService.requireAuthenticated(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Authentication required", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when session is expired")
    void shouldThrowExceptionWhenSessionIsExpired() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", "expired")});
        when(cookieService.getTokenFromCookies(any())).thenReturn("expired");
        when(authSessionRepository.findByTokenAndExpiresAtAfter(eq("expired"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> authContextService.requireAuthenticated(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Session expired", exception.getMessage());
    }

    @Test
    @DisplayName("Should return admin from administrator requirement")
    void shouldReturnAdminFromAdministratorRequirement() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Employee employee = buildEmployee(EmployeeRole.ADMINISTRATOR);
        AuthSession session = AuthSession.builder()
                .id(1L)
                .token("token-admin")
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", "token-admin")});
        when(cookieService.getTokenFromCookies(any())).thenReturn("token-admin");
        when(authSessionRepository.findByTokenAndExpiresAtAfter(eq("token-admin"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        Employee result = authContextService.requireAdministrator(request);

        assertEquals(EmployeeRole.ADMINISTRATOR, result.getRole());
    }

    @Test
    @DisplayName("Should throw exception when user is not admin")
    void shouldThrowExceptionWhenUserIsNotAdmin() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Employee employee = buildEmployee(EmployeeRole.WAREHOUSEMAN);
        AuthSession session = AuthSession.builder()
                .id(1L)
                .token("token-user")
                .employee(employee)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AUTH_TOKEN", "token-user")});
        when(cookieService.getTokenFromCookies(any())).thenReturn("token-user");
        when(authSessionRepository.findByTokenAndExpiresAtAfter(eq("token-user"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(session));

        ApiException exception = assertThrows(ApiException.class,
                () -> authContextService.requireAdministrator(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Administrator access required", exception.getMessage());
    }

    private Employee buildEmployee(EmployeeRole role) {
        return Employee.builder()
                .id(1L)
                .firstName("System")
                .lastName("Administrator")
                .position("Administrator")
                .role(role)
                .username("admin")
                .passwordHash("x")
                .build();
    }
}
