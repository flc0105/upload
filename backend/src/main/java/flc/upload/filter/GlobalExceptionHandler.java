package flc.upload.filter;

import flc.upload.exception.BusinessException;
import flc.upload.exception.VerifyFailedException;
import flc.upload.model.Result;
import flc.upload.util.CookieUtil;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.ResponseUtil;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(VerifyFailedException.class)
    public Result<?> handleException(VerifyFailedException e, HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.removeCookie("token", request, response);
        return ResponseUtil.buildErrorResult(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleException(BusinessException e) {
        return ResponseUtil.buildErrorResult(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return ResponseUtil.buildErrorResult(InternationalizationUtil.translate("unknown.error") + ": " + e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String defaultMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return new Result<>(false, "参数错误：" + defaultMessage);
    }
}
