package ru.mtuci.coursemanagement.util;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

public class CsrfUtil {

    private static final String CSRF_TOKEN = "CSRF_TOKEN";

    public static String getToken(HttpSession session) {
        Object t = session.getAttribute(CSRF_TOKEN);
        if (t == null) {
            String token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_TOKEN, token);
            return token;
        }
        return t.toString();
    }

    public static boolean check(HttpSession session, String tokenFromRequest) {
        if (tokenFromRequest == null) return false;
        return getToken(session).equals(tokenFromRequest);
    }
}
