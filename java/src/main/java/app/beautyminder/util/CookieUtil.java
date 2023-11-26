package app.beautyminder.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

public class CookieUtil {

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }

    // Production
    public void addSecureCookie(HttpServletResponse response, String name, String value, int maxAge, boolean isSecure, String sameSite) {
        StringBuilder cookieValue = new StringBuilder(name).append("=").append(value)
                .append("; Max-Age=").append(maxAge)
                .append("; Path=/")
                .append("; HttpOnly");

        if (isSecure) {
            cookieValue.append("; Secure");
        }

        if (sameSite != null) {
            cookieValue.append("; SameSite=").append(sameSite); // Lax, Strict
        }

        response.addHeader("Set-Cookie", cookieValue.toString());
    }
}

