package flc.upload.util;

import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射工具类，提供对 Java 反射的封装和常用操作。
 */
public class ReflectionUtil {

    /**
     * 根据字段名称获取类中的字段。
     *
     * @param fieldName 要获取的字段名称
     * @param clazz     要获取字段的类
     * @return 如果找到匹配的字段，则返回该字段对象；如果未找到匹配的字段，则返回null
     */
    public static Field getFieldByName(String fieldName, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // 如果在当前类中未找到字段，则在父类中查找
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getFieldByName(fieldName, superclass);
            }
        }
        return null;
    }

    /**
     * 获取对象的类属性及其对应的值。
     *
     * @param obj 要获取属性的对象
     * @return 包含类属性及其对应值的映射表
     */
    public static Map<String, Object> getClassAttributes(Object obj) {
        Map<String, Object> attributeMap = new HashMap<>();
        Class<?> clazz = obj.getClass();
        // 获取父类的所有声明字段
        Field[] fields = clazz.getSuperclass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                attributeMap.put(field.getName(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return attributeMap;
    }

    /**
     * 从方法的 @ApiOperation 注解获取接口名称
     *
     * @param method 方法
     * @return 接口名称，如果未指定名称则返回空字符串
     */
    public static String getApiName(Method method) {
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            return apiOperation.value();
        } else {
            return Strings.EMPTY;
        }
    }

    /**
     * 获取连接点方法的参数数组（排除 HttpServletRequest 和 HttpServletResponse）。
     *
     * @param joinPoint 连接点对象
     * @return 参数数组的字符串表示
     */
    public static String getMethodArguments(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        return Arrays.toString(Arrays.stream(args)
                .filter(e -> !(e instanceof HttpServletRequest) && !(e instanceof HttpServletResponse))
                .toArray(Object[]::new));
    }

}
