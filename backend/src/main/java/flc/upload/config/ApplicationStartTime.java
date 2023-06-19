package flc.upload.config;

public class ApplicationStartTime {
    private static final ApplicationStartTime instance = new ApplicationStartTime();
    private final long startTime;

    private ApplicationStartTime() {
        startTime = System.currentTimeMillis();
    }

    public static ApplicationStartTime getInstance() {
        return instance;
    }

    public long getUptime() {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime;
    }
}