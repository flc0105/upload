package flc.upload.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@PropertySource("classpath:app-config.properties")
public class AppConfig {

    @Value("${preview.max-file-size}")
    private int previewMaxFileSize;

    @Value("${download.buffer-size}")
    private int downloadBufferSize;

    @Value("#{'${private.directories}'.split(',')}")
    private List<String> privateDirectories;

    @Value("${preview.compress-image}")
    private boolean previewCompressImage;

    @Value("${deactivated.tokens}")
    private List<String> deactivatedTokens;
    @Value("${request.timeout}")
    private int requestTimeout;

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;
    @Value("${jwt.expiration-time}")
    private int jwtExpirationTime;

    @Value("${log.max-size}")
    private int logMaxSize;

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public int getJwtExpirationTime() {
        return jwtExpirationTime;
    }

    public void setJwtExpirationTime(int jwtExpirationTime) {
        this.jwtExpirationTime = jwtExpirationTime;
    }

    public List<String> getDeactivatedTokens() {
        return deactivatedTokens;
    }

    public void setDeactivatedTokens(List<String> deactivatedTokens) {
        this.deactivatedTokens = deactivatedTokens;
    }

    public int getPreviewMaxFileSize() {
        return previewMaxFileSize;
    }

    public void setPreviewMaxFileSize(int previewMaxFileSize) {
        this.previewMaxFileSize = previewMaxFileSize;
    }

    public int getDownloadBufferSize() {
        return downloadBufferSize;
    }

    public void setDownloadBufferSize(int downloadBufferSize) {
        this.downloadBufferSize = downloadBufferSize;
    }

    public List<String> getPrivateDirectories() {
        return privateDirectories;
    }

    public void setPrivateDirectories(List<String> privateDirectories) {
        this.privateDirectories = privateDirectories;
    }

    public boolean isPreviewCompressImage() {
        return previewCompressImage;
    }

    public void setPreviewCompressImage(boolean previewCompressImage) {
        this.previewCompressImage = previewCompressImage;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getLogMaxSize() {
        return logMaxSize;
    }

    public void setLogMaxSize(int logMaxSize) {
        this.logMaxSize = logMaxSize;
    }
}
