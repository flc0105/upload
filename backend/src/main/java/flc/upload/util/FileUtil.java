package flc.upload.util;

import flc.upload.model.MyConfigProperties;
import info.monitorenter.cpdetector.io.*;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.tika.Tika;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static MyConfigProperties myConfigProperties;

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

    public static long calculateDirectorySize(File file) {
        long size = 0;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    size += calculateDirectorySize(child);
                }
            }
        } else {
            size = file.length();
        }

        return size;
    }

    public static int countFiles(File file) {
        int count = 0;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isFile()) {
                        count++;
                    } else if (child.isDirectory()) {
                        count += countFiles(child);
                    }
                }
            }
        } else if (file.isFile()) {
            count++;
        }

        return count;
    }

    public static int countFolders(File file) {
        int count = 0;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isDirectory()) {
                        count++;
                        count += countFolders(child);
                    }
                }
            }
        }
        return count;
    }

    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log10(size) / Math.log10(1024));

        double formattedSize = size / Math.pow(1024, unitIndex);
        String formattedSizeString = String.format("%.2f", formattedSize);
        return formattedSizeString + " " + units[unitIndex];
    }

    public static String getCreationTime(File file) {
        try {
            Path path = Paths.get(file.toURI());
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attributes.creationTime();
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return creationDateTime.format(formatter);
        } catch (Exception e) {
            logger.error("获取文件创建时间失败：" + e.getMessage());
            return "null";
        }
    }

    public static String getModifiedTime(File file) {
        try {
            Path path = Paths.get(file.toURI());
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime lastModifiedTime = attributes.lastModifiedTime();
            LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return lastModifiedDateTime.format(formatter);
        } catch (Exception e) {
            logger.error("获取文件创建时间失败：" + e.getMessage());
            return "null";
        }
    }

    public static String getImageSize(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        return width + "x" + height;
    }

    public static String getImageInfo(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append("--------- 图片信息 ---------").append("\n");
        try {
            sb.append("分辨率: ").append(getImageSize(file)).append("\n");
            ImageMetadata metadata = Imaging.getMetadata(file);
            if (metadata instanceof JpegImageMetadata) {
                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                TiffField captureTime = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                if (captureTime != null) {
                    sb.append("拍摄时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(captureTime.getStringValue()))).append("\n");
                }
                TiffField cameraMaker = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MAKE);
                if (cameraMaker != null) {
                    sb.append("相机制造商: ").append(cameraMaker.getStringValue()).append("\n");
                }
                TiffField cameraModel = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MODEL);
                if (cameraModel != null) {
                    sb.append("相机型号: ").append(cameraModel.getStringValue()).append("\n");
                }
                TiffField programName = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_SOFTWARE);
                if (programName != null) {
                    sb.append("程序名称: ").append(programName.getStringValue()).append("\n");
                }
            }
        } catch (Exception e) {
            logger.error("获取图片元数据失败：" + e.getMessage());
        }
        return sb.toString();
    }

    public static String getAudioInfo(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append("--------- 音乐信息 ---------").append("\n");
        try {
            AudioFile audio = AudioFileIO.read(file);
            sb.append("时长: ").append(audio.getAudioHeader().getTrackLength()).append(" 秒\n");
            Tag tag = audio.getTag();
            if (tag != null) {
                sb.append("艺术家: ").append(tag.getFirst(FieldKey.ARTIST)).append("\n");
                sb.append("标题: ").append(tag.getFirst(FieldKey.TITLE)).append("\n");
                sb.append("专辑: ").append(tag.getFirst(FieldKey.ALBUM)).append("\n");
                sb.append("注释: ").append(tag.getFirst(FieldKey.COMMENT)).append("\n");
            }
        } catch (Exception e) {
            logger.error("获取音乐元数据失败：" + e.getMessage());
        }
        return sb.toString();
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return ""; // 文件没有扩展名
        }
        return fileName.substring(dotIndex);
    }

    @Autowired
    public void setMyConfigProperties(MyConfigProperties myConfigProperties) {
        FileUtil.myConfigProperties = myConfigProperties;
    }

}