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

    @Value("${preview.maxFileSize}")
    private int maxFileSize;

    @Value("${download.bufferSize}")
    private int bufferSize;

    @Value("#{'${private.directories}'.split(',')}")
    private List<String> privateDirectories;

    @Value("${preview.compressImage}")
    private boolean compressImage;

    @Value("${deactivated.tokens}")
    private List<String> deactivatedTokens;
    @Value("${bookmark.timeout}")
    private int bookmarkTimeout;

    public List<String> getDeactivatedTokens() {
        return deactivatedTokens;
    }
//
//    @Value("${password}")
//    private String password;

    public void setDeactivatedTokens(List<String> deactivatedTokens) {
        this.deactivatedTokens = deactivatedTokens;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public List<String> getPrivateDirectories() {
        return privateDirectories;
    }

    public void setPrivateDirectories(List<String> privateDirectories) {
        this.privateDirectories = privateDirectories;
    }

    public boolean isCompressImage() {
        return compressImage;
    }

    public void setCompressImage(boolean compressImage) {
        this.compressImage = compressImage;
    }

    public int getBookmarkTimeout() {
        return bookmarkTimeout;
    }

    public void setBookmarkTimeout(int bookmarkTimeout) {
        this.bookmarkTimeout = bookmarkTimeout;
    }

    //    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
}
