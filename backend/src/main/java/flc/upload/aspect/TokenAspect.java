package flc.upload.aspect;

import flc.upload.manager.TokenManager;
import flc.upload.util.CookieUtil;
import flc.upload.util.InternationalizationUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

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
        ServletRequestAttributes attributes = Objects.requireNonNull((ServletRequestAttributes) RequestContextHolder.getRequestAttributes(), InternationalizationUtil.translate("get.request.information.failure"));
        HttpServletRequest request = attributes.getRequest();
        String token = CookieUtil.getCookie("token", request);
        tokenManager.verify(token);
    }
}
