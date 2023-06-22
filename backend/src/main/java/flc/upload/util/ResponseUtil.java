package flc.upload.util;

import flc.upload.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ResponseUtil {

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        ResponseUtil.messageSource = messageSource;
    }


    public static <T> Result<T> buildSuccessResult(String successKey, T data) {
        String successMessage = messageSource.getMessage(successKey, null, LocaleContextHolder.getLocale());
        return new Result<>(true, successMessage, data);
    }

    public static <T> Result<T> buildSuccessResult(String successKey) {
        String successMessage = messageSource.getMessage(successKey, null, LocaleContextHolder.getLocale());
        return new Result<>(true, successMessage);
    }


    public static <T> Result<T> buildErrorResult(String errorKey) {
        String errorMessage = messageSource.getMessage(errorKey, null, LocaleContextHolder.getLocale());
        return new Result<>(false, errorMessage);
    }

    public static <T> Result<T> buildErrorResult(String errorKey, T data) {
        String errorMessage = messageSource.getMessage(errorKey, null, LocaleContextHolder.getLocale());
        return new Result<>(false, errorMessage, data);
    }

    public static String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
