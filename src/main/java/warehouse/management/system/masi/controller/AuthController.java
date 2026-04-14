package warehouse.management.system.masi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.management.system.masi.request.EmployeeRequest;
import warehouse.management.system.masi.request.LoginRequest;
import warehouse.management.system.masi.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody EmployeeRequest request,
                                      HttpServletRequest httpRequest) {
        return authService.register(request, httpRequest);
    }

    @GetMapping("/is-authorized")
    public ResponseEntity<?> isAuthorized(HttpServletRequest request) {
        return authService.isAuthorized(request);
    }
}
