package flc.upload.util;

import info.monitorenter.cpdetector.io.*;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static String relativize(String uploadPath, File f) {
        String path = new File(uploadPath).toURI().relativize(f.toURI()).getPath();
        return "/" + (path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
    }

    public static Collection<Path> find(String uploadPath, PathMatcher matcher) throws Exception {
        try (Stream<Path> files = Files.walk(Paths.get(uploadPath))) {
            return files
                    .filter(matcher::matches)
                    .collect(Collectors.toList());
        }
    }

    public static void downloadFile(HttpServletResponse response, File file) throws IOException {
        String type = new MimetypesFileTypeMap().getContentType(file.getName());
        response.setHeader("Content-type", type);
        String fileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    public static void deleteFolder(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] list = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    deleteFolder(f);
                }
                file.delete();
            }
        }
    }

    public static void createZip(ZipOutputStream zos, File file, String dir) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        String zipName = file.getName();
        if (dir != null && !dir.isEmpty()) {
            zipName = dir + "/" + file.getName();
        }
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                createZip(zos, f, zipName);
            }
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(file);
            zos.putNextEntry(new ZipEntry(zipName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    public static String getFileEncode(File file) {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance());
        Charset charset = null;
        try {
            charset = detector.detectCodepage(file.toURI().toURL());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (charset != null)
            return charset.name();
        else
            return null;
    }
}