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

    @Value("${jwt.secret-key}")
    @ApiModelProperty(value = "JWT密钥")
    private String jwtSecretKey;

    @Value("${jwt.expiration-time}")
    @ApiModelProperty(value = "JWT过期时间（秒）")
    private int jwtExpirationTime;

    @Value("${download.buffer.size}")
    @ApiModelProperty(value = "下载缓冲区大小")
    private int downloadBufferSize;

    @Value("${preview.max.size}")
    @ApiModelProperty(value = "预览最大文件大小")
    private int previewMaxSize;

    @Value("${preview.image.compress}")
    @ApiModelProperty(value = "预览图片时是否压缩")
    private boolean previewImageCompress;

    @Value("${private.directories}")
    @ApiModelProperty(value = "私密目录列表")
    private List<String> privateDirectories;

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

    public int getDownloadBufferSize() {
        return downloadBufferSize;
    }

    public void setDownloadBufferSize(int downloadBufferSize) {
        this.downloadBufferSize = downloadBufferSize;
    }

    public int getPreviewMaxSize() {
        return previewMaxSize;
    }

    public void setPreviewMaxSize(int previewMaxSize) {
        this.previewMaxSize = previewMaxSize;
    }

    public boolean isPreviewImageCompress() {
        return previewImageCompress;
    }

    public void setPreviewImageCompress(boolean previewImageCompress) {
        this.previewImageCompress = previewImageCompress;
    }

    public List<String> getPrivateDirectories() {
        return privateDirectories;
    }

    public void setPrivateDirectories(List<String> privateDirectories) {
        this.privateDirectories = privateDirectories;
    }
}
