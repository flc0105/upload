package flc.upload.aspect;

import flc.upload.enums.PermissionType;
import flc.upload.manager.PermissionManager;
import flc.upload.manager.TokenManager;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.util.CookieUtil;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.RequestUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 权限切面
 */
@Aspect
@Component
public class PermissionAspect {
    private final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    private final TokenManager tokenManager;

    private final PermissionManager permissionManager;

    @Autowired
    public PermissionAspect(TokenManager tokenManager, PermissionManager permissionManager) {
        this.tokenManager = tokenManager;
        this.permissionManager = permissionManager;
    }

    /**
     * 在方法执行前进行权限检查
     *
     * @throws Throwable 当无法获取到request信息时抛出异常
     */
    @Before("@annotation(flc.upload.annotation.Permission)")
    public void before() throws Throwable {
        ServletRequestAttributes attributes = Objects.requireNonNull((ServletRequestAttributes) RequestContextHolder.getRequestAttributes(), InternationalizationUtil.translate("get.request.information.failure"));
        HttpServletRequest request = attributes.getRequest();
        String path = RequestUtil.getRelativeRequestURI(request);

        Result<?> result;
        try {
            result = Objects.requireNonNull(permissionManager.get(path));
        } catch (Exception e) {
            logger.warn("无法获取到该接口的权限信息，默认放行：" + path);
            return;
        }

        Permission permission = (Permission) result.getDetail();
        if (permission.getIsAdmin().equals(PermissionType.ADMIN.getValue())) {
            String token = CookieUtil.getCookie("token", request);
            tokenManager.verify(token);
        }
    }
}
