package flc.upload.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import flc.upload.model.Result;
import flc.upload.util.CookieUtil;
import flc.upload.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 切面类
 */
@Aspect
@Component
public class LogAspect {

    // private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
    private static Logger logger;

    public static final List<Map> logs = new ArrayList<>();

    @Pointcut("@annotation(flc.upload.annotation.Log)")
    public void logPointCut() {

    }

    private String hashMapToJson(Map<String, String> hashMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(hashMap);
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前方法的名称和参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        logger = LoggerFactory.getLogger(method.getDeclaringClass());
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.nanoTime();

        // 执行目标方法，并记录返回值
        Object result = joinPoint.proceed();

        long endTime = System.nanoTime();

        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = ((HttpServletRequest) arg);
                String ip = request.getRemoteAddr();
                if (Objects.equals(ip, "0:0:0:0:0:0:0:1")) {
                    ip = "localhost";
                }
                String userAgent = request.getHeader("User-Agent");
                String browser = "";
                String os = "";
                if (userAgent != null && !userAgent.isEmpty()) {
                    if (userAgent.contains("Chrome")) {
                        browser = "Chrome";
                    } else if (userAgent.contains("Firefox")) {
                        browser = "Firefox";
                    } else if (userAgent.contains("Safari")) {
                        browser = "Safari";
                    }
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
                String methodArguments = Arrays.toString(Arrays.stream(args)
                        .filter(e -> !(e instanceof HttpServletRequest) && !(e instanceof HttpServletResponse))
                        .toArray(Object[]::new));

                logger.info("客户端 {} 执行了 {} 方法，参数为 {}，执行结果为 {}，浏览器为 {}，操作系统为 {}", ip, methodName, methodArguments, (result instanceof Result) ? ((Result) result).isSuccess() : null, browser, os);

                Map<String, String> logMap = new LinkedHashMap<>();
                String contextPath = request.getContextPath();
                String requestURI = request.getRequestURI();
                String path = requestURI.substring(contextPath.length());
                logMap.put("请求地址", path);
                logMap.put("ip", ip);
                logMap.put("是否执行成功", String.valueOf((result instanceof Result) ? ((Result<?>) result).isSuccess() : null));
                String className = signature.getDeclaringTypeName();
                logMap.put("浏览器", browser);
                logMap.put("操作系统", os);
                logMap.put("请求方式", request.getMethod());
                logMap.put("操作时间", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                logMap.put("类名", className);
                logMap.put("方法名", methodName);
                logMap.put("请求参数", methodArguments);
                logMap.put("参数类型", request.getContentType());
                logMap.put("具体消息", String.valueOf((result instanceof Result) ? ((Result<?>) result).getMsg() : null));
                logMap.put("返回结果", result.toString());
                logMap.put("方法耗时", ((endTime - startTime) / 1_000_000) + " ms");
                logMap.put("token", CookieUtil.getCookie("token", request));
                logMap.put("username", JwtUtil.getUsername(CookieUtil.getCookie("token", request)));

                String json = hashMapToJson(logMap);
                logs.add(logMap);


                logger.info("\n" + json);
                break;
            }
        }
        return result;
    }
}