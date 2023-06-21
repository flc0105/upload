package flc.upload.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 切面类
 */
@Aspect
@Component
public class LogAspect {

    public static final List<Map<String, String>> logs = new ArrayList<>();

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
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Map<String, String> logMap = new LinkedHashMap<>();
            logMap.put("操作时间", CommonUtil.getCurrentDate());
            logMap.put("接口名称", CommonUtil.getApiName(method));
            logMap.put("请求地址", CommonUtil.getRequestURL(request));
            logMap.put("IP", CommonUtil.getIp(request));
            logMap.put("是否执行成功", String.valueOf((result instanceof Result) ? ((Result<?>) result).isSuccess() : null));
            logMap.put("浏览器", CommonUtil.getBrowser(request));
            logMap.put("操作系统", CommonUtil.getOS(request));
            logMap.put("请求方式", request.getMethod());
            logMap.put("类名", signature.getDeclaringTypeName());
            logMap.put("方法名", method.getName());
            logMap.put("请求参数", CommonUtil.getMethodArguments(joinPoint));
            logMap.put("参数类型", request.getContentType());
            logMap.put("具体消息", String.valueOf((result instanceof Result) ? ((Result<?>) result).getMsg() : null));
//            logMap.put("返回结果", result.toString());
            logMap.put("方法耗时", ((endTime - startTime) / 1_000_000) + " ms");
            logMap.put("Token", CookieUtil.getCookie("token", request));
            logMap.put("设备", JwtUtil.getUsername(CookieUtil.getCookie("token", request)));
            logs.add(logMap);
            logger.info("\n" + hashMapToJson(logMap));
        }
        return result;
    }
}