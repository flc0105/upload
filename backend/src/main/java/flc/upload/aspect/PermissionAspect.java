package flc.upload.aspect;

import flc.upload.enums.PermissionType;
import flc.upload.manager.PermissionManager;
import flc.upload.manager.TokenManager;
import flc.upload.model.Permission;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
import flc.upload.util.CookieUtil;
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
public class PermissionAspect {

    private final TokenManager tokenManager;

    private final PermissionManager permissionManager;

    @Autowired
    public PermissionAspect(TokenManager tokenManager, PermissionManager permissionManager) {
        this.tokenManager = tokenManager;
        this.permissionManager = permissionManager;
    }

    @Before("@annotation(flc.upload.annotation.Permission)")
    public void before() throws Throwable {
        ServletRequestAttributes attributes = Objects.requireNonNull((ServletRequestAttributes) RequestContextHolder.getRequestAttributes(), "无法获取到request信息");
        HttpServletRequest request = attributes.getRequest();
        String path = CommonUtil.getRequestURL(request);
        Result<?> result = Objects.requireNonNull(permissionManager.getPermission(path), "无法获取到该接口的权限信息");
        Permission permission = (Permission) result.getDetail();
        if (permission.getIsAdmin().equals(PermissionType.ADMIN.getValue())) {
            String token = CookieUtil.getCookie("token", request);
            tokenManager.verify(token);
        }
    }
}
