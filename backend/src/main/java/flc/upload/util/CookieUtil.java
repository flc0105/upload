package flc.upload.util;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie 工具类，提供对 HTTP Cookie 的操作和常用方法。
 */
public class CookieUtil {
    /**
     * 根据名称从HttpServletRequest中获取对应的Cookie值。
     *
     * @param name    要获取的Cookie的名称
     * @param request HttpServletRequest对象
     * @return 如果找到对应的Cookie，则返回其值；否则返回null
     */
    public static String getCookie(String name, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 向HttpServletResponse中添加一个Cookie。
     *
     * @param name     要设置的Cookie的名称
     * @param value    要设置的Cookie的值
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     */
    public static void addCookie(String name, String value, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
    }

    /**
     * 从HttpServletRequest中删除指定名称的Cookie。
     *
     * @param name     要删除的Cookie的名称
     * @param request  HttpServletRequest对象
     * @param response HttpServletResponse对象
     */
    public static void removeCookie(String name, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
