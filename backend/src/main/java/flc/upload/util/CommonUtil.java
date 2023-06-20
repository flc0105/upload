package flc.upload.util;

import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {
    /**
     * 格式化当前时间
     *
     * @return 日期时间字符串
     */
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 获取类属性的值
     *
     * @param fieldName 属性名
     * @param clazz     类
     * @return 属性
     */
    public static Field getFieldByName(String fieldName, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getFieldByName(fieldName, superclass);
            }
        }
        return null;
    }

    /**
     * 获取类的所有属性
     *
     * @param obj 对象
     * @return 属性和值的map
     */
    public static Map<String, Object> getClassAttributes(Object obj) {
        Map<String, Object> attributeMap = new HashMap<>();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                attributeMap.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return attributeMap;
    }

    /**
     * 格式化毫秒
     *
     * @param milliseconds 毫秒
     * @return 格式化后的时间字符串
     */
    public static String convertTime(long milliseconds) {

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;

        String result = "";
        if (days > 0) {
            result += days + "天 ";
        }
        if (hours > 0) {
            result += hours + "小时 ";
        }
        if (minutes > 0) {
            result += minutes + "分钟 ";
        }
        if (seconds > 0) {
            result += seconds + "秒";
        }

        return result;
    }

    /**
     * 使用正则表达式解析 SQLite URL 获取文件路径
     *
     * @param sqliteUrl SQLite的JDBC URL
     * @return 文件路径
     */
    public static String getSQLiteFilePath(String sqliteUrl) {
        Pattern pattern = Pattern.compile("jdbc:sqlite:(.+)");
        Matcher matcher = pattern.matcher(sqliteUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 获取内存大小
     *
     * @return 内存大小（字节）
     * @throws Exception
     */
    public static long getTotalPhysicalMemorySize() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return Long.parseLong(mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize").toString());
//        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        long physicalMemorySize = osBean.getTotalPhysicalMemorySize(); //osBean.getFreePhysicalMemorySize();
    }

    /**
     * 获取内存剩余
     *
     * @return 内存剩余（字节）
     * @throws Exception
     */
    public static long getFreePhysicalMemorySize() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return Long.parseLong(mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "FreePhysicalMemorySize").toString());
    }


    /**
     * 从方法的@ApiOperation注解获取接口名称
     *
     * @param method 方法
     * @return 接口名称
     */
    public static String getApiName(Method method) {
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            return apiOperation.value();
        } else {
            return "null";
        }
    }


    /**
     * 从request中获取IP地址
     *
     * @param request
     * @return IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (Objects.equals(ip, "0:0:0:0:0:0:0:1")) {
            ip = "localhost";
        }
        return ip;
    }

    /**
     * 从request中获取浏览器
     *
     * @param request
     * @return 浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
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
     * 从request中获取操作系统
     *
     * @param request
     * @return 操作系统
     */
    public static String getOS(HttpServletRequest request) {
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
     * 获取方法参数
     *
     * @param joinPoint
     * @return 参数列表字符串
     */
    public static String getMethodArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return Arrays.toString(Arrays.stream(args)
                .filter(e -> !(e instanceof HttpServletRequest) && !(e instanceof HttpServletResponse))
                .toArray(Object[]::new));
    }

    /**
     * 获取请求地址的相对路径
     *
     * @param request
     * @return 请求地址的相对路径
     */
    public static String getRequestURL(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        return requestURI.substring(contextPath.length());
    }

}
