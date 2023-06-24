package flc.upload.util;

import flc.upload.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 响应工具类，用于构建标准的响应结果对象。
 * 提供了构建成功结果和错误结果的方法，可根据消息源获取国际化的消息。
 */
@Component
public class ResponseUtil {

    private static MessageSource messageSource;

    /**
     * 注入消息源实例。
     *
     * @param messageSource 消息源实例
     */
    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        ResponseUtil.messageSource = messageSource;
    }

    /**
     * 构建成功的结果对象，不带数据。
     *
     * @param successKey 成功消息的键
     * @param <T>        结果数据的类型
     * @return 成功的结果对象
     */
    public static <T> Result<T> buildSuccessResult(String successKey) {
        String successMessage = messageSource.getMessage(successKey, null, LocaleContextHolder.getLocale());
        return new Result<>(true, successMessage);
    }

    /**
     * 构建成功的结果对象，带数据。
     *
     * @param successKey 成功消息的键
     * @param data       结果数据
     * @param <T>        结果数据的类型
     * @return 成功的结果对象
     */
    public static <T> Result<T> buildSuccessResult(String successKey, T data) {
        String successMessage = messageSource.getMessage(successKey, null, LocaleContextHolder.getLocale());
        return new Result<>(true, successMessage, data);
    }

    /**
     * 构建错误的结果对象，不带数据。
     *
     * @param errorKey 错误消息的键
     * @param <T>      结果数据的类型
     * @return 错误的结果对象
     */
    public static <T> Result<T> buildErrorResult(String errorKey) {
        String errorMessage = messageSource.getMessage(errorKey, null, LocaleContextHolder.getLocale());
        return new Result<>(false, errorMessage);
    }

    /**
     * 构建错误的结果对象，带数据。
     *
     * @param errorKey 错误消息的键
     * @param data     结果数据
     * @param <T>      结果数据的类型
     * @return 错误的结果对象
     */
    public static <T> Result<T> buildErrorResult(String errorKey, T data) {
        String errorMessage = messageSource.getMessage(errorKey, null, LocaleContextHolder.getLocale());
        return new Result<>(false, errorMessage, data);
    }
}
