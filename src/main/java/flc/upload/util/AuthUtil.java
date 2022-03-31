package flc.upload.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Scanner;

public class AuthUtil {

    private static String getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void addCookie(String name, String value, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private static void removeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public static boolean verifyToken(HttpServletRequest request, HttpServletResponse response) {
        String token = getCookie(request);
        if (token == null) {
            return false;
        }
        File file = new File("token");
        if (!file.isFile()) {
            removeCookie(response);
            return false;
        }
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(token)) {
                    return true;
                }
            }
            removeCookie(response);
            return false;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

}
