package flc.upload.aspect;

import flc.upload.annotation.Permission;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class PermissionAspect {

    @Pointcut("@annotation(permission)")
    public void annotatedWithPermission(Permission permission) {
    }

    @Around("annotatedWithPermission(permission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, Permission permission) throws Throwable {
        String permissionValue = Arrays.toString(permission.value());
        if (permissionValue.equals("admin")) {
        }
        return joinPoint.proceed();
    }
}
