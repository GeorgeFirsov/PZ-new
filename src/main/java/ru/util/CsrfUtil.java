package ru.mtuci.coursemanagement.util;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public class CsrfUtil {

    private static final String CSRF_TOKEN = "CSRF_TOKEN";

    public static String getToken(HttpSession session) {
        Object token = session.getAttribute(CSRF_TOKEN);
        if (token == null) {
            String newToken = UUID.randomUUID().toString();
            session.setAttribute(CSRF_TOKEN, newToken);
            return newToken;
        }
        return token.toString();
    }

    public static boolean checkToken(HttpSession session, String tokenFromRequest) {
        if (tokenFromRequest == null) return false;
        String token = getToken(session);
        return token.equals(tokenFromRequest);
    }
}
