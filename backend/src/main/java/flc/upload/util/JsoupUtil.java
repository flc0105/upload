package flc.upload.util;

import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Jsoup 工具类，提供对 Jsoup HTML 解析库的封装和常用操作。
 */
@Component
public class JsoupUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsoupUtil.class);

    /**
     * 获取指定URL的页面标题。
     *
     * @param url 要获取标题的URL
     * @return 页面的标题，如果获取失败则返回空字符串
     */
    public static String getTitle(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.title();
        } catch (IOException e) {
            logger.error("获取页面标题失败：" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

    /**
     * 从给定的URL中获取图标的URL，并转换为Base64编码。
     * 如果网页中包含.ico或.svg类型的图标链接，则将其转换为Base64编码。
     * 如果无法找到合适的图标链接，则尝试使用默认的网站favicon.ico链接。
     *
     * @param bookmarkUrl 网页URL
     * @return 图标的Base64编码字符串，如果获取失败则返回null
     */
    public static String getIcon(String bookmarkUrl) {
        try {
            // 从给定的URL中获取网页的Document对象
            Document document = Jsoup.connect(bookmarkUrl).get();

            // 查找.ico类型的图标链接并转换为Base64编码
            Element elementIco = document.head().select("link[href~=.*\\.ico]").first();
            if (elementIco != null) {
                return convertIconToBase64(elementIco.absUrl("href"));
            }

            // 查找.svg类型的图标链接并转换为Base64编码
            Element elementSvg = document.head().select("link[href~=.*\\.svg]").first();
            if (elementSvg != null) {
                return convertSvgToBase64(elementSvg.absUrl("href"));
            }

            // 使用默认的网站favicon.ico链接并转换为Base64编码
            URL url = new URL(bookmarkUrl);
            return convertIconToBase64(url.getProtocol() + "://" + url.getAuthority() + "/favicon.ico");

        } catch (Exception e) {
            logger.error("获取图标失败：{}, {}", bookmarkUrl, e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * 将给定的SVG文件URL转换为Base64编码字符串。
     *
     * @param svgUrl SVG文件URL
     * @return SVG文件的Base64编码字符串，如果转换失败则返回null
     */
    public static String convertSvgToBase64(String svgUrl) {
        try {
            URL url = new URL(svgUrl);

            // 读取SVG源码
            StringBuilder svgSource = new StringBuilder();
            try (InputStream in = url.openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    svgSource.append(line);
                }
            }

            // 将SVG源码转换为Base64编码
            byte[] svgBytes = svgSource.toString().getBytes(StandardCharsets.UTF_8);
            byte[] base64Bytes = Base64.getEncoder().encode(svgBytes);
            return "data:image/svg+xml;base64," + new String(base64Bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("SVG 转换 Base64 失败：{}, {}", svgUrl, e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * 将给定的图标URL转换为Base64编码字符串。
     *
     * @param iconUrl 图标URL
     * @return 缩放后的图标的Base64编码字符串，如果转换失败则返回null
     */
    public static String convertIconToBase64(String iconUrl) {
        try {
            URL url = new URL(iconUrl);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = url.openStream()) {
                byte[] bytes = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(bytes)) > 0) {
                    out.write(bytes, 0, bytesRead);
                }
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (Exception e) {
            logger.error("下载 ico 或转换 Base64 时出错：{}, {}", iconUrl, e.getLocalizedMessage());
        }
        return null;
    }

}
