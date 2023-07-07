package flc.upload.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.*;
import org.apache.ibatis.cache.CacheKey;
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
import java.util.*;

/**
 * 切面类，用于记录日志
 */
@Aspect
@Component
public class LogAspect {

    /**
     * 存储日志信息的列表
     */
    public static final List<Map<String, String>> logs = new ArrayList<>();

//    public static final Map<CacheKey, Long> cachedApis = new HashMap<>();

    /**
     * 定义切入点，标记使用了 @Log 注解的方法
     */
    @Pointcut("@annotation(flc.upload.annotation.Log)")
    public void logPointCut() {
    }

    private static AppConfig appConfig;

    /**
     * 注入 AppConfig
     *
     * @param appConfig AppConfig 实例
     */
    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        LogAspect.appConfig = appConfig;
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

    public static boolean isElapsed(long startTime) {
        long elapsedTime = System.nanoTime() - startTime;
        long elapsedSeconds = elapsedTime / 1_000_000_000; // 将纳秒转换为秒

        return elapsedSeconds > 10; // 如果经过的秒数超过10秒，则返回true
    }

    /**
     * 添加日志到列表
     *
     * @param map    存储日志信息的 Map
     * @param logger Logger 实例
     * @throws JsonProcessingException JSON 解析异常
     */
    private void addLog(Map<String, String> map, Logger logger) throws JsonProcessingException {
//        String api = map.get("request.url"); // 获取接口地址
//        String param = map.get("request.parameters"); // 获取请求参数
//        CacheKey cacheKey = new CacheKey(api, param);
//        Long lastVisited = cachedApis.get(cacheKey);// 上次访问时间
//        if (lastVisited != null && !isElapsed(lastVisited)) { //如果不是首次访问且距离上次访问不超过10s，不记录日志
//            return;
//        }

        if (logs.size() >= appConfig.getLogMaxSize()) { // 如果日志数量大于配置的数量，从最前面开始删除
            logs.remove(0);
        }

        logs.add(map); // 添加日志
//        cachedApis.put(cacheKey, System.nanoTime()); // 添加完成后缓存该接口
        logger.info("\n" + CommonUtil.toJsonString(InternationalizationUtil.translateMapKeys(map)));


    }


}
//
//class CacheKey {
//    private String requestUrl;
//
//    private int paramHash;
//
//    public CacheKey(String requestUrl, Object param) {
//        this.requestUrl = requestUrl;
//        this.paramHash = Objects.hash(param);
//    }
//
//    public String getRequestUrl() {
//        return requestUrl;
//    }
//
//    public void setRequestUrl(String requestUrl) {
//        this.requestUrl = requestUrl;
//    }
//
//    public int getParamHash() {
//        return paramHash;
//    }
//
//    public void setParamHash(int paramHash) {
//        this.paramHash = paramHash;
//    }
//
//    // 重写 hashCode() 和 equals() 方法
//    @Override
//    public int hashCode() {
//        return Objects.hash(requestUrl, paramHash);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null || getClass() != obj.getClass()) {
//            return false;
//        }
//        CacheKey other = (CacheKey) obj;
//        return Objects.equals(requestUrl, other.requestUrl) && paramHash == other.paramHash;
//    }
//
//    @Override
//    public String toString() {
//        return "CacheKey{" +
//                "requestUrl='" + requestUrl + '\'' +
//                ", paramHash=" + paramHash +
//                '}';
//    }
//}