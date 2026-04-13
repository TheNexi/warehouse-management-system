package warehouse.management.system.masi.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

    @Value("${auth.token.name:AUTH_TOKEN}")
    private String tokenCookieName;

    @Value("${auth.token.max-age:43200}")
    private int tokenMaxAgeSeconds;

    @Value("${auth.token.secure:false}")
    private boolean tokenCookieSecure;

    private static final String COOKIE_PATH = "/";

    public void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createCookie(token, tokenMaxAgeSeconds);
        response.addCookie(cookie);
    }

    public void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie("", 0);
        response.addCookie(cookie);
    }

    public String getTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (tokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private Cookie createCookie(String value, int maxAge) {
        Cookie cookie = new Cookie(tokenCookieName, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(tokenCookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
