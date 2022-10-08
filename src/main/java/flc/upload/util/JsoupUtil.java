package flc.upload.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsoupUtil {
    public static String getTitle(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.title();
        } catch (IOException e) {
            System.err.println(e.toString());
            return "";
        }
    }

    public static String getIcon(String url) {
        try {
            Document document = Jsoup.connect(url).get();
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

    public static String fileToBase64(String iconUrl) throws MalformedURLException {
        URL url = new URL(iconUrl);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = url.openStream()) {
            byte[] bytes = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(bytes)) > 0) {
                out.write(bytes, 0, bytesRead);
            }
            return new String(Base64.encodeBase64(out.toByteArray()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println(e.toString());
            return "";
        }
    }
}
