package flc.upload.util;

import flc.upload.exception.BusinessException;
import flc.upload.model.AppConfig;
import info.monitorenter.cpdetector.io.*;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.logging.log4j.util.Strings;
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
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类，提供常用的文件操作方法。
 */
@Component
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        FileUtil.appConfig = appConfig;
    }

    /**
     * 获取相对路径。
     * 将给定的文件路径相对于上传路径进行相对化，并返回相对路径字符串。
     *
     * @param uploadPath 上传文件的基础路径
     * @param file       要相对化的文件
     * @return 相对路径字符串
     */
    public static String relativize(String uploadPath, File file) {
        String path = new File(uploadPath).toURI().relativize(file.toURI()).getPath();
        return "/" + (path.endsWith("/") ? path.substring(0, path.length() - 1) : path);
    }

    /**
     * 检测文件类型。
     * 使用 Apache Tika 库来检测给定文件的类型，并返回类型字符串。
     *
     * @param file 要检测类型的文件
     * @return 文件类型字符串
     */
    public static String detectFileType(File file) {
        try {
            return new Tika().detect(file);
        } catch (IOException e) {
            logger.error("" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

    /**
     * 递归删除指定文件或目录及其子目录和文件。
     *
     * @param file 要删除的文件或目录
     */
    public static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            if (!file.delete()) {
                logger.error("Failed to delete file: " + file.getAbsolutePath());
            }
            return;
        }
        File[] list = file.listFiles();
        if (list != null) {
            for (File f : list) {
                deleteRecursively(f);
            }
        }
        if (!file.delete()) {
            logger.error("Failed to delete directory: " + file.getAbsolutePath());
        }
    }

    /**
     * 设置下载文件的响应头信息。
     *
     * @param file     要下载的文件
     * @param response HTTP响应对象
     */
    public static void setDownloadHeaders(File file, HttpServletResponse response) {
        // 获取文件的MIME类型并设置到响应头的"Content-type"字段
        response.setHeader("Content-type", new MimetypesFileTypeMap().getContentType(file.getName()));
        // 获取文件的长度并设置到响应头的"Content-Length"字段
        response.setHeader("Content-Length", String.valueOf(file.length()));
        // 对文件名进行UTF-8编码，然后设置到响应头的"Content-Disposition"字段
        String encodedFileName = UriUtils.encode(file.getName(), "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"");
        // 将"Content-Disposition"响应头暴露给前端，以便客户端可以读取该响应头信息
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 下载指定文件到HTTP响应中。
     *
     * @param file     要下载的文件
     * @param response HTTP响应对象
     * @throws IOException 下载过程中可能抛出的IO异常
     */
    public static void download(File file, HttpServletResponse response) throws IOException {
        setDownloadHeaders(file, response);
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            // Java 9: in.transferTo(out);
            int bufferSize = Math.max(appConfig.getFileDownloadBufferSize(), 1024); // 根据配置获取合适的缓冲区大小
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
    }

    /**
     * 根据给定的路径参数获取文件对象。
     *
     * @param first 第一个路径参数
     * @param more  额外的路径参数
     * @return 表示文件的File对象
     */
    public static File getFile(String first, String... more) {
        return Paths.get(first, more).toFile();
    }

    /**
     * 将压缩后的图像文件下载到HTTP响应中。
     *
     * @param file     要下载的图像文件
     * @param response HTTP响应对象
     * @throws IOException 下载过程中可能抛出的IO异常
     */
    public static void downloadCompressedImage(File file, HttpServletResponse response) throws IOException {
        setDownloadHeaders(file, response);
        OutputStream out = response.getOutputStream();
        String fileType = detectFileType(file);
        if (Objects.equals(fileType, "image/jpeg")) {
            // 对JPEG图像文件进行压缩并输出到响应流中
            Thumbnails.of(file).scale(1f).outputQuality(0.5f).toOutputStream(out);
        } else if (Objects.equals(fileType, "image/png")) {
            // 将PNG文件转换为JPG格式并进行压缩，并将结果输出到响应流中
            String jpgName = CommonUtil.generateUUID() + ".jpg";
            Thumbnails.of(file).scale(1f).toFile(jpgName);
            Thumbnails.of(jpgName).scale(1f).outputQuality(0.5f).toOutputStream(out);
            logger.info("自动删除转换的 jpg 文件 {}：{}", jpgName, new File(jpgName).delete());
        } else {
            throw new BusinessException("unsupported.file.type");
        }
    }

    /**
     * 递归将文件及其子目录压缩到Zip输出流中。
     *
     * @param zos  Zip输出流对象
     * @param file 要压缩的文件或目录
     * @param dir  相对于Zip文件的目录路径
     */
    public static void zipRecursively(ZipOutputStream zos, File file, String dir) {
        String zipName = file.getName();
        if (dir != null && !dir.isEmpty()) {
            zipName = dir + File.separator + file.getName();
        }
        if (file.isDirectory()) {
            // 如果是目录，则递归处理目录下的文件和子目录
            for (File f : Objects.requireNonNull(file.listFiles())) {
                zipRecursively(zos, f, zipName);
            }
        } else {
            // 如果是文件，则将文件写入到Zip输出流中
            byte[] buffer = new byte[1024];
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(zipName));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            } catch (IOException e) {
                logger.error("压缩文件时出错：" + e.getLocalizedMessage());
            }
        }
    }

    /**
     * 获取文件的字符编码。
     *
     * @param file 要获取编码的文件
     * @return 文件的字符编码，如果无法确定编码或出现异常，则返回null
     */
    public static String getFileEncode(File file) {
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();
        detector.add(new ParsingDetector(false));
        detector.add(JChardetFacade.getInstance());
        detector.add(ASCIIDetector.getInstance());
        detector.add(UnicodeDetector.getInstance());
        try {
            return detector.detectCodepage(file.toURI().toURL()).name();
        } catch (Exception e) {
            logger.error("获取文件编码时出错：" + e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * 计算目录的总大小（包括子目录和文件）。
     *
     * @param file 目录
     * @return 目录的总大小（以字节为单位）
     */
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
            // 如果是文件，则直接获取文件大小
            size = file.length();
        }
        return size;
    }

    /**
     * 计算目录中文件的总数（包括子目录中的文件）。
     *
     * @param file 目录
     * @return 目录中文件的总数
     */
    public static int countFiles(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isFile()) {
                        // 如果是文件，则增加计数
                        count++;
                    } else if (child.isDirectory()) {
                        // 如果是目录，则递归调用countFiles方法，累加子目录中的文件数
                        count += countFiles(child);
                    }
                }
            }
        } else if (file.isFile()) {
            // 如果是文件，则增加计数
            count++;
        }

        return count;
    }

    /**
     * 计算目录中文件夹的总数（包括子目录中的文件夹）。
     *
     * @param file 目录
     * @return 目录中文件夹的总数
     */
    public static int countFolders(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (child.isDirectory()) {
                        // 如果是文件夹，则增加计数
                        count++;
                        // 递归调用countFolders方法，累加子目录中的文件夹数
                        count += countFolders(child);
                    }
                }
            }
        }
        return count;
    }

    /**
     * 格式化文件大小，将字节数转换为易读的字符串表示。
     *
     * @param size 文件大小（以字节为单位）
     * @return 格式化后的文件大小字符串
     */
    public static String formatSize(long size) {
        // 如果文件大小小于等于0字节，则返回"0 B"表示文件大小为0
        if (size <= 0) {
            return "0 B";
        }
        // 定义一个单位数组，用于表示文件大小的不同单位（B、KB、MB、GB、TB）
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        // 计算文件大小对应的单位索引，通过以1024为底的对数计算
        int unitIndex = (int) (Math.log10(size) / Math.log10(1024));
        // 将文件大小除以对应单位的幂次方，得到格式化后的大小
        double formattedSize = size / Math.pow(1024, unitIndex);
        // 使用String.format方法将格式化后的大小转换为字符串，并保留两位小数
        String formattedSizeString = String.format("%.2f", formattedSize);
        // 将格式化后的大小字符串和单位拼接在一起，返回最终的文件大小字符串
        return formattedSizeString + " " + units[unitIndex];
    }

    /**
     * 获取文件的创建时间。
     *
     * @param file 文件
     * @return 文件的创建时间字符串
     */
    public static String getCreationTime(File file) {
        try {
            Path path = Paths.get(file.toURI());
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime creationTime = attributes.creationTime();
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return creationDateTime.format(formatter);
        } catch (Exception e) {
            logger.error("获取文件创建时间失败：" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

    /**
     * 获取文件的修改时间。
     *
     * @param file 文件
     * @return 文件的修改时间字符串
     */
    public static String getModifiedTime(File file) {
        try {
            Path path = Paths.get(file.toURI());
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime lastModifiedTime = attributes.lastModifiedTime();
            LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return lastModifiedDateTime.format(formatter);
        } catch (Exception e) {
            logger.error("获取文件修改时间失败：" + e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

    /**
     * 获取图像的尺寸。
     *
     * @param file 图像文件
     * @return 图像的尺寸字符串，格式为"宽度x高度"
     * @throws IOException 读取图像文件时可能抛出的异常
     */
    public static String getImageSize(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        return width + " x " + height;
    }

    /**
     * 获取文件的扩展名。
     *
     * @param file 文件
     * @return 文件的扩展名（包括点号），如果文件没有扩展名则返回空字符串
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return ""; // 文件没有扩展名
        }
        return fileName.substring(dotIndex);
    }

    /**
     * 判断给定的文件是否为图像文件。
     *
     * @param file 要判断的文件
     * @return 如果文件是图像文件，则返回 true；否则返回 false
     */
    public static boolean isImage(File file) {
        return detectFileType(file).startsWith("image");
    }

    /**
     * 检查给定文件是否为音频文件。
     *
     * @param file 要检查的文件
     * @return 如果文件是音频文件，则返回 true；否则返回 false
     */
    public static boolean isAudio(File file) {
        return detectFileType(file).startsWith("audio");
    }

    /**
     * 获取指定标签的 Exif 值。
     *
     * @param metadata 图像的元数据对象
     * @param tagInfo  要获取的 Exif 标签
     * @return 指定标签的 Exif 值
     * @throws ImageReadException 如果读取图像元数据出错
     */
    public static String getExif(JpegImageMetadata metadata, TagInfo tagInfo) throws ImageReadException {
        return Objects.requireNonNull(metadata.findEXIFValue(tagInfo)).getStringValue();
    }

    /**
     * 获取图片的相关信息，并以键值对形式返回。
     *
     * @param file 图片文件
     * @return 包含图片信息的键值对
     */
    public static Map<String, String> getImageInfo(File file) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            map.put("image.resolution", getImageSize(file));
            ImageMetadata imageMetadata = Imaging.getMetadata(file);
            if (imageMetadata instanceof JpegImageMetadata) {
                JpegImageMetadata metadata = (JpegImageMetadata) imageMetadata;
                String captureTime = getExif(metadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(captureTime));
                map.put("capture.time", formattedTime);
                map.put("camera.maker", getExif(metadata, TiffTagConstants.TIFF_TAG_MAKE));
                map.put("camera.model", getExif(metadata, TiffTagConstants.TIFF_TAG_MODEL));
                map.put("program.name", getExif(metadata, TiffTagConstants.TIFF_TAG_SOFTWARE));
            }
        } catch (Exception e) {
            logger.error("获取图片信息失败：" + e.getLocalizedMessage());
        }
        return map;
    }

    /**
     * 获取音频文件的相关信息。
     *
     * @param file 音频文件
     * @return 包含音频信息的映射表
     */
    public static Map<String, String> getAudioInfo(File file) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            AudioFile audio = AudioFileIO.read(file);
            map.put("audio.length", CommonUtil.formatSeconds(audio.getAudioHeader().getTrackLength()));
            Tag tag = Objects.requireNonNull(audio.getTag());
            map.put("audio.artist", tag.getFirst(FieldKey.ARTIST));
            map.put("audio.title", tag.getFirst(FieldKey.TITLE));
            map.put("audio.album", tag.getFirst(FieldKey.ALBUM));
            map.put("audio.comment", tag.getFirst(FieldKey.COMMENT));
        } catch (Exception e) {
            logger.error("获取音频信息失败：" + e.getLocalizedMessage());
        }
        return map;
    }

    public static String calculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;

            try (FileInputStream fis = new FileInputStream(filePath)) {
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }

            byte[] mdBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte mdByte : mdBytes) {
                sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            logger.error("获取文件哈希出错：{}", e.getLocalizedMessage());
            return Strings.EMPTY;
        }
    }

}