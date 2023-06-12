package flc.upload.util;

import flc.upload.model.AppConfig;
import flc.upload.model.MyConfigProperties;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

@Component
public class JsoupUtil {
    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        JsoupUtil.appConfig = appConfig;
    }

    public static String getTitle(String url) {
        System.out.println("param:" + appConfig.getBookmarkTimeout());
        try {
            Document document = Jsoup.connect(url).timeout(appConfig.getBookmarkTimeout()).get();
            return document.title();
        } catch (IOException e) {
            System.err.println(e.toString());
            return "";
        }
    }

    public static String getIcon(String url) {
        try {
            Document document = Jsoup.connect(url).timeout(appConfig.getBookmarkTimeout()).get();
            Element element = document.head().select("link[href~=.*\\.ico]").first();
            if (element != null) {
                return element.absUrl("href");
            } else {
                return null;
            }
        } catch (IOException e) {
            System.err.println(e.toString());
            return null;
        }
    }

    public static String fileToBase64(String iconUrl) throws IOException {
        URL url = new URL(iconUrl);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(appConfig.getBookmarkTimeout());
        connection.setReadTimeout(appConfig.getBookmarkTimeout());

//        try (InputStream in = url.openStream()) {
        try (InputStream in = connection.getInputStream()) {
            byte[] bytes = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(bytes)) > 0) {
                out.write(bytes, 0, bytesRead);
            }
            return new String(Base64.encodeBase64(out.toByteArray()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e);
            return "";
        }
    }
}
