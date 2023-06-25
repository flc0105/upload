package flc.upload.exception;

import flc.upload.util.InternationalizationUtil;

public class BusinessException extends RuntimeException {

    private String message;

    public BusinessException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return InternationalizationUtil.translate(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
