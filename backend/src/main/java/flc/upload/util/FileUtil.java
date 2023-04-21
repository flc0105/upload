package flc.upload.util;

import flc.upload.model.MyConfigProperties;
import info.monitorenter.cpdetector.io.*;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static MyConfigProperties myConfigProperties;

    @Autowired
    public void setMyConfigProperties(MyConfigProperties myConfigProperties) {
        FileUtil.myConfigProperties = myConfigProperties;
    }

    public static String relativize(String uploadPath, File file) {
        String path = new File(uploadPath).toURI().relativize(file.toURI()).getPath();
        return "/" + (path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
    }

    public static String formatDate(long date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    public static String detectFileType(File file) {
        try {
            return new Tika().detect(file);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }

    public static void deleteRecursively(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] list = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    deleteRecursively(f);
                }
                file.delete();
            }
        }
    }

    public static void download(File file, HttpServletResponse response) throws Exception {
        response.setHeader("Content-type", new MimetypesFileTypeMap().getContentType(file.getName()));
        response.setHeader("Content-Length", "" + file.length());
//        String filename = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
//        response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\"");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + UriUtils.encode(file.getName(), "UTF-8") + "\"");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();

        int bufferSize = myConfigProperties.getBufferSize();
        if (bufferSize == 0) {
            bufferSize = 1024;
        }

        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    public static void zipFile(ZipOutputStream zos, File file, String dir) {
        String zipName = file.getName();
        if (dir != null && !dir.isEmpty()) {
            zipName = dir + File.separator + file.getName();
        }
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                zipFile(zos, f, zipName);
            }
        } else {
            byte[] buffer = new byte[1024];
            try {
                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(zipName));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    public static String getFileEncode(File file) {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance());
        try {
            return detector.detectCodepage(file.toURI().toURL()).name();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
    }
}