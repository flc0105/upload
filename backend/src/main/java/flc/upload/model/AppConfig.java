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

//    @Value("${deactivated.tokens}")
//    @ApiModelProperty(value = "已停用的Token列表")
//    private List<String> deactivatedTokens;

//    @Value("${cors.allowed-origins}")
//    @ApiModelProperty(value = "允许的跨域来源列表")
//    private List<String> corsAllowedOrigins;
//


    @Value("${jwt.secret-key}")
    @ApiModelProperty(value = "JWT密钥")
    private String jwtSecretKey;

    @Value("${jwt.expiration-time}")
    @ApiModelProperty(value = "JWT过期时间（秒）")
    private int jwtExpirationTime;

    @Value("${file.download.buffer-size}")
    @ApiModelProperty(value = "下载缓冲区大小")
    private int fileDownloadBufferSize;

    @Value("${file.preview.max-size}")
    @ApiModelProperty(value = "预览最大文件大小")
    private int filePreviewMaxSize;

    @Value("${file.preview.compress-image}")
    @ApiModelProperty(value = "预览图片时是否压缩")
    private boolean filePreviewCompressImage;

    //    @Value("#{'${file.private-dir}'.split(',')}")
    @Value("${file.private-dir}")
    @ApiModelProperty(value = "私密目录列表")
    private List<String> filePrivateDir;

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

    public int getFileDownloadBufferSize() {
        return fileDownloadBufferSize;
    }

    public void setFileDownloadBufferSize(int fileDownloadBufferSize) {
        this.fileDownloadBufferSize = fileDownloadBufferSize;
    }

    public int getFilePreviewMaxSize() {
        return filePreviewMaxSize;
    }

    public void setFilePreviewMaxSize(int filePreviewMaxSize) {
        this.filePreviewMaxSize = filePreviewMaxSize;
    }

    public boolean isFilePreviewCompressImage() {
        return filePreviewCompressImage;
    }

    public void setFilePreviewCompressImage(boolean filePreviewCompressImage) {
        this.filePreviewCompressImage = filePreviewCompressImage;
    }

    public List<String> getFilePrivateDir() {
        return filePrivateDir;
    }

    public void setFilePrivateDir(List<String> filePrivateDir) {
        this.filePrivateDir = filePrivateDir;
    }
}
