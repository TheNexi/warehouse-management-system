package warehouse.management.system.masi.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = CookieService.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "auth.token.name=AUTH_TOKEN",
        "auth.token.max-age=3600",
        "auth.token.secure=false"
})
class CookieServiceTest {

    @Autowired
    private CookieService cookieService;

    @MockitoBean
    private HttpServletResponse response;

    @Test
    @DisplayName("Should set token cookie with correct properties")
    void shouldSetTokenCookie() {
        String token = "test-token-123";
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        cookieService.setTokenCookie(response, token);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("AUTH_TOKEN", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure());
        assertEquals("/", cookie.getPath());
        assertEquals(3600, cookie.getMaxAge());
    }

    @Test
    @DisplayName("Should clear token cookie")
    void shouldClearTokenCookie() {
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        cookieService.clearTokenCookie(response);

        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("AUTH_TOKEN", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
    }
}
