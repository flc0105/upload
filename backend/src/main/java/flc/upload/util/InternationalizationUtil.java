package flc.upload.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 国际化工具类，提供对多语言支持和国际化资源的管理。
 */
@Component
public class InternationalizationUtil {

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        InternationalizationUtil.messageSource = messageSource;
    }

    public static String translate(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    private static Object[] argsToArray(Object... args) {
        return args;
    }

    public static String translate(String key, Object... args) {
        return messageSource.getMessage(key, argsToArray(args), LocaleContextHolder.getLocale());
    }

    public static Map<String, String> translateMapKeys(Map<String, String> map) {
        Map<String, String> localizedMap = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            localizedMap.put(translate(key), map.get(key));
        }
        return localizedMap;
    }
}
