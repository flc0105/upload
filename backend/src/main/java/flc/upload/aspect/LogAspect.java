package flc.upload.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import flc.upload.manager.LogManager;
import flc.upload.model.Result;
import flc.upload.util.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 切面类，用于记录日志
 */
@Aspect
@Component
public class LogAspect {
    /**
     * 定义切入点，标记使用了 @Log 注解的方法
     */
    @Pointcut("@annotation(flc.upload.annotation.Log)")
    public void logPointCut() {
    }


    private final LogManager logManager;

    @Autowired
    public LogAspect(LogManager logManager) {
        this.logManager = logManager;
    }


    /**
     * 环绕通知，在被 @Log 注解标记的方法执行前后记录日志
     *
     * @param joinPoint ProceedingJoinPoint 对象
     * @return 目标方法的执行结果
     * @throws Throwable 抛出的异常
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法的签名信息，包括方法名、参数类型等
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 获取当前方法对象
        Method method = signature.getMethod();

        // 使用方法所在类的类名作为 Logger 的名称
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());

        // 创建一个有序的 Map，用于存储日志的键值对信息
        Map<String, String> logMap = new LinkedHashMap<>();

        // 获取请求的上下文信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes(), InternationalizationUtil.translate("get.request.information.failure"));

        // 获取当前的 HttpServletRequest 对象
        HttpServletRequest request = attributes.getRequest();

        // 填充日志信息
        logMap.put("operation.time", CommonUtil.getCurrentDate());
        logMap.put("api.name", ReflectionUtil.getApiName(method));
        logMap.put("request.url", RequestUtil.getRelativeRequestURI(request));
        logMap.put("ip.address", RequestUtil.getClientIpAddress(request));
        logMap.put("browser", RequestUtil.getClientBrowser(request));
        logMap.put("operating.system", RequestUtil.getClientOS(request));
        logMap.put("request.method", request.getMethod());
        logMap.put("class.name", signature.getDeclaringTypeName());
        logMap.put("method.name", method.getName());
        logMap.put("request.parameters", ReflectionUtil.getMethodArguments(joinPoint));
        logMap.put("parameter.type", request.getContentType());
        logMap.put("token", CommonUtil.toJsonString(JwtUtil.getTokenInfo(CookieUtil.getCookie("token", request))));
        logMap.put("referer", request.getHeader("Referer"));

        long startTime = System.nanoTime();
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            long endTime = System.nanoTime();

            // 添加执行成功的日志信息
            logMap.put("success", String.valueOf((result instanceof Result) ? ((Result<?>) result).isSuccess() : null));
            logMap.put("specific.message", String.valueOf((result instanceof Result) ? ((Result<?>) result).getMsg() : null));
            logMap.put("execution.time", ((endTime - startTime) / 1_000_000) + " ms");

            // 添加日志到列表
            addLog(logMap, logger);

            return result;
        } catch (Throwable throwable) {
            long endTime = System.nanoTime();

            // 添加执行失败的日志信息
            logMap.put("success", String.valueOf(false));
            logMap.put("specific.message", throwable.getLocalizedMessage());
            logMap.put("execution.time", ((endTime - startTime) / 1_000_000) + " ms");


            // 添加日志到列表
            addLog(logMap, logger);

            // 抛出异常
            throw throwable;
        }
    }

    /**
     * 添加日志到列表
     *
     * @param map    存储日志信息的 Map
     * @param logger Logger 实例
     * @throws JsonProcessingException JSON 解析异常
     */
    private void addLog(Map<String, String> map, Logger logger) throws JsonProcessingException {
        logManager.add(map);
        logger.info("\n" + CommonUtil.toJsonString(InternationalizationUtil.translateMapKeys(map)));
    }

}
