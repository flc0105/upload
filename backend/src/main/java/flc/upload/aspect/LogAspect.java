package flc.upload.aspect;

import flc.upload.annotation.Log;
import flc.upload.manager.OperationLogManager;
import flc.upload.model.OperationLog;
import flc.upload.model.Result;
import flc.upload.util.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
public class LogAspect {

    private final OperationLogManager operationLogManager;

    @Autowired
    public LogAspect(OperationLogManager operationLogManager) {
        this.operationLogManager = operationLogManager;
    }

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        // 获取方法的签名信息，包括方法名、参数类型等
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 获取当前方法对象
        Method method = signature.getMethod();

        // 使用方法所在类的类名作为 Logger 的名称
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());

        // 获取请求的上下文信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes(), InternationalizationUtil.translate("get.request.information.failure"));

        // 获取当前的 HttpServletRequest 对象
        HttpServletRequest request = attributes.getRequest();
        String token = CookieUtil.getCookie("token", request);

        OperationLog operationLog = new OperationLog();
        operationLog.setOperationTime(CommonUtil.getCurrentDate());
        operationLog.setApiName(ReflectionUtil.getApiName(method));
        operationLog.setRequestUrl(RequestUtil.getRelativeRequestURI(request));
        operationLog.setIpAddress(RequestUtil.getClientIpAddress(request));
        operationLog.setBrowser(RequestUtil.getClientBrowser(request));
        operationLog.setOperatingSystem(RequestUtil.getClientOS(request));
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setClassName(signature.getDeclaringTypeName());
        operationLog.setMethodName(method.getName());
        operationLog.setRequestParameter(ReflectionUtil.getMethodArguments(joinPoint));
        operationLog.setToken(token);
        operationLog.setParameterType(request.getContentType());

        long startTime = System.nanoTime();
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            long endTime = System.nanoTime();

            operationLog.setSuccess(result instanceof Result && ((Result<?>) result).isSuccess());
            operationLog.setMessage(result instanceof Result ? ((Result<?>) result).getMsg() : null);
            operationLog.setExecutionTime(((endTime - startTime) / 1_000_000) + " ms");

            operationLogManager.add(operationLog);
            logger.info(operationLog.toString());

            return result;
        } catch (Throwable throwable) {
            long endTime = System.nanoTime();

            operationLog.setSuccess(false);
            operationLog.setMessage(throwable.getLocalizedMessage());
            operationLog.setExecutionTime(((endTime - startTime) / 1_000_000) + " ms");

            operationLogManager.add(operationLog);
            logger.error(operationLog.toString());
            throw throwable;
        }
    }

}
