package ru.mtuci.coursemanagement.security;

import jakarta.servlet.http.HttpSession;

import java.util.UUID;

public class CsrfTokenUtil {

    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        String token = (String) session.getAttribute("_csrf");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("_csrf", token);
        }
        return token;
    }

    public static boolean isTokenValid(HttpSession session, String tokenFromRequest) {
        if (session == null) {
            return false;
        }
        if (tokenFromRequest == null) {
            return false;
        }
        String tokenInSession = (String) session.getAttribute("_csrf");
        return tokenFromRequest.equals(tokenInSession);
    }

    private CsrfTokenUtil() {
    }
}
