package warehouse.management.system.masi.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import warehouse.management.system.masi.model.AuthSession;
import warehouse.management.system.masi.model.Employee;
import warehouse.management.system.masi.model.enums.EmployeeRole;
import warehouse.management.system.masi.repository.AuthSessionRepository;
import warehouse.management.system.masi.repository.EmployeeRepository;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.response.LoginResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthService.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private AuthSessionRepository authSessionRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private AuthContextService authContextService;

    @MockitoBean
    private HttpServletResponse response;

    @MockitoBean
    private HttpServletRequest request;

    @Test
    @DisplayName("Should login user successfully")
    void shouldLoginUserSuccessfully() {
        LoginRequest loginRequest = createLoginRequest();
        Employee employee = createEmployee();
        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);

        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        ResponseEntity<?> result = authService.login(loginRequest, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertInstanceOf(LoginResponse.class, result.getBody());
        verify(employeeRepository).findByUsername("admin");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(authSessionRepository).save(sessionCaptor.capture());
        verify(cookieService).setTokenCookie(response, sessionCaptor.getValue().getToken());

        AuthSession savedSession = sessionCaptor.getValue();
        assertNotNull(savedSession.getToken());
        assertEquals(employee.getUsername(), savedSession.getEmployee().getUsername());
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        LoginRequest loginRequest = createLoginRequest();

        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> authService.login(loginRequest, response));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        LoginRequest loginRequest = createLoginRequest();
        Employee employee = createEmployee();

        when(employeeRepository.findByUsername("admin")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> authService.login(loginRequest, response));
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Should logout user successfully")
    void shouldLogoutUserSuccessfully() {
        when(cookieService.getTokenFromCookies(any())).thenReturn("session-token");

        ResponseEntity<?> result = authService.logout(request, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(cookieService).getTokenFromCookies(any());
        verify(authSessionRepository).deleteByToken("session-token");
        verify(cookieService).clearTokenCookie(response);
    }

    @Test
    @DisplayName("Should validate authorized session")
    void shouldValidateAuthorizedSession() {
        ResponseEntity<?> result = authService.isAuthorized(request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(authContextService).requireAuthenticated(request);
    }

    private LoginRequest createLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password123");
        return request;
    }

    private Employee createEmployee() {
        return Employee.builder()
                .id(1L)
                .firstName("System")
                .lastName("Administrator")
                .position("Administrator")
                .role(EmployeeRole.ADMINISTRATOR)
                .username("admin")
                .passwordHash("hashedPassword")
                .build();
    }
}
