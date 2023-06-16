package flc.upload.aspect;

import flc.upload.manager.TokenManager;
import flc.upload.util.CookieUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class TokenAspect {

    private final TokenManager tokenManager;

    @Autowired
    public TokenAspect(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Before("@annotation(flc.upload.annotation.Token)")
    public void before(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = ((HttpServletRequest) arg);
                String token = CookieUtil.getCookie("token", request);
                tokenManager.verify(token);
                logger.info("验证成功");
            }
        }
    }
}
