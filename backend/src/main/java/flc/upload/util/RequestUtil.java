package flc.upload.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 请求工具类，提供了获取客户端信息和请求URI的方法。
 */
public class RequestUtil {
    /**
     * 获取请求的客户端 IP 地址。
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (Objects.equals(ip, "0:0:0:0:0:0:0:1")) {
            ip = "localhost";
        }
        return ip;
    }

    /**
     * 获取客户端使用的浏览器信息。
     *
     * @param request HTTP 请求对象
     * @return 客户端浏览器信息
     */
    public static String getClientBrowser(HttpServletRequest request) {
        String browser = "";
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            if (userAgent.contains("Chrome")) {
                browser = "Chrome";
            } else if (userAgent.contains("Firefox")) {
                browser = "Firefox";
            } else if (userAgent.contains("Safari")) {
                browser = "Safari";
            } else {
                browser = userAgent;
            }
        }
        return browser;
    }

    /**
     * 获取客户端操作系统信息。
     *
     * @param request HTTP 请求对象
     * @return 客户端操作系统信息
     */
    public static String getClientOS(HttpServletRequest request) {
        String os = "";
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && !userAgent.isEmpty()) {
            if (userAgent.toLowerCase().contains("iphone")) {
                os = "iOS";
            } else if (userAgent.toLowerCase().contains("mac")) {
                os = "Mac";
            } else if (userAgent.toLowerCase().contains("windows")) {
                os = "Windows";
            } else if (userAgent.toLowerCase().contains("android")) {
                os = "Android";
            } else if (userAgent.toLowerCase().contains("x11") || userAgent.toLowerCase().contains("linux")) {
                os = "Unix/Linux";
            } else {
                os = "Other";
            }
        }
        return os;
    }

    /**
     * 获取相对于上下文路径的请求URI。
     *
     * @param request HTTP请求对象
     * @return 相对于上下文路径的请求URI
     */
    public static String getRelativeRequestURI(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        return requestURI.substring(contextPath.length());
    }

}
