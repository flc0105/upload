package flc.upload.aspect;

import flc.upload.model.Result;
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
import java.util.Arrays;
import java.util.Objects;

/**
 * 切面类
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("@annotation(flc.upload.annotation.Log)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前方法的名称和参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();
        // 执行目标方法，并记录返回值
        Object result = joinPoint.proceed();
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
                logger.info("客户端{}执行了{}方法，参数为{}，执行结果为{}，浏览器为{}，操作系统为{}", ip, methodName, methodArguments, result != null ? ((Result) result).isSuccess() : null, browser, os);
                break;
            }
        }
        return result;
    }
}