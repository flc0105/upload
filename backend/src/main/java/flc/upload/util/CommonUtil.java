package flc.upload.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtil {
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
