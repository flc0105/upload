package flc.upload.exception;

public class VerifyFailedException extends RuntimeException {
    private String message;

    public VerifyFailedException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
