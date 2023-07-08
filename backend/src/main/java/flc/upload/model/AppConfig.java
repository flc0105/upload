package flc.upload.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@ApiModel(description = "应用程序配置")
@Component
@Configuration
@PropertySource("classpath:app-config.properties")
public class AppConfig {

    @Value("${cors.allowed-origins}")
    @ApiModelProperty(value = "允许的跨域来源列表")
    private List<String> corsAllowedOrigins;

    @Value("${deactivated.tokens}")
    @ApiModelProperty(value = "已停用的Token列表")
    private List<String> deactivatedTokens;

    @Value("${download.buffer-size}")
    @ApiModelProperty(value = "下载缓冲区大小")
    private int downloadBufferSize;

    @Value("${jwt.secret-key}")
    @ApiModelProperty(value = "JWT密钥")
    private String jwtSecretKey;

    @Value("${jwt.expiration-time}")
    @ApiModelProperty(value = "JWT过期时间（秒）")
    private int jwtExpirationTime;

    @Value("${log.max-size}")
    @ApiModelProperty(value = "日志最大数量")
    private int logMaxSize;

    @Value("${preview.max-file-size}")
    @ApiModelProperty(value = "预览最大文件大小")
    private int previewMaxFileSize;

    @Value("${preview.compress-image}")
    @ApiModelProperty(value = "预览时是否压缩图像")
    private boolean previewCompressImage;

    @Value("#{'${private.directories}'.split(',')}")
    @ApiModelProperty(value = "私有目录列表")
    private List<String> privateDirectories;

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public List<String> getDeactivatedTokens() {
        return deactivatedTokens;
    }

    public void setDeactivatedTokens(List<String> deactivatedTokens) {
        this.deactivatedTokens = deactivatedTokens;
    }

    public int getDownloadBufferSize() {
        return downloadBufferSize;
    }

    public void setDownloadBufferSize(int downloadBufferSize) {
        this.downloadBufferSize = downloadBufferSize;
    }

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

    public int getLogMaxSize() {
        return logMaxSize;
    }

    public void setLogMaxSize(int logMaxSize) {
        this.logMaxSize = logMaxSize;
    }

    public int getPreviewMaxFileSize() {
        return previewMaxFileSize;
    }

    public void setPreviewMaxFileSize(int previewMaxFileSize) {
        this.previewMaxFileSize = previewMaxFileSize;
    }

    public boolean isPreviewCompressImage() {
        return previewCompressImage;
    }

    public void setPreviewCompressImage(boolean previewCompressImage) {
        this.previewCompressImage = previewCompressImage;
    }

    public List<String> getPrivateDirectories() {
        return privateDirectories;
    }

    public void setPrivateDirectories(List<String> privateDirectories) {
        this.privateDirectories = privateDirectories;
    }
}
