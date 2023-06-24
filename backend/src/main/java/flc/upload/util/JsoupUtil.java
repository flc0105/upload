package flc.upload.util;

import flc.upload.model.AppConfig;
import org.apache.logging.log4j.util.Strings;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Jsoup 工具类，提供对 Jsoup HTML 解析库的封装和常用操作。
 */
@Component
public class JsoupUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsoupUtil.class);

    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        JsoupUtil.appConfig = appConfig;
    }

    /**
     * 获取指定URL的页面标题。
     *
     * @param url 要获取标题的URL
     * @return 页面的标题，如果获取失败则返回空字符串
     */
    public static String getTitle(String url) {
        try {
            Document document = Jsoup.connect(url).timeout(appConfig.getRequestTimeout()).get();
            return document.title();
        } catch (IOException e) {
            logger.error("获取页面标题失败：" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

    /**
     * 获取指定URL的网站图标。
     *
     * @param url 要获取图标的URL
     * @return 网站的图标URL，如果获取失败则返回null
     */
    public static String getIcon(String url) {
        try {
            Document document = Jsoup.connect(url).timeout(appConfig.getRequestTimeout()).get();
            Element element = document.head().select("link[href~=.*\\.ico]").first();
            if (element != null) {
                return element.absUrl("href");
            }
        } catch (IOException e) {
            logger.error("获取网站图标失败：" + e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 将图标文件转换为Base64编码字符串。
     *
     * @param iconUrl 图标文件的URL
     * @return Base64编码的图标字符串，如果转换失败则返回空字符串
     */
    public static String convertIconToBase64(String iconUrl) {
        try {
            URL url = new URL(iconUrl);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(appConfig.getRequestTimeout());
            try (InputStream in = connection.getInputStream()) {
                byte[] bytes = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(bytes)) > 0) {
                    out.write(bytes, 0, bytesRead);
                }
                return new String(Base64.encodeBase64(out.toByteArray()), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("转换图标失败：" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }
}
