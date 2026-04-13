package warehouse.management.system.masi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("Should provide Argon2 password encoder")
    void shouldProvideArgon2PasswordEncoder() {
        String encoded = passwordEncoder.encode("password123");

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$argon2"));
    }

    @Test
    @DisplayName("Should load security filter chain")
    void shouldLoadSecurityFilterChain() {
        assertNotNull(securityFilterChain);
    }
}
