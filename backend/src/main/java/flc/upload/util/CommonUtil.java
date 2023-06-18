package flc.upload.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static Field getFieldByName(String fieldName, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getFieldByName(fieldName, superclass);
            }
        }
        return null;
    }
}
