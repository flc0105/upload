package flc.upload.util;

import flc.upload.model.MyConfigProperties;
import info.monitorenter.cpdetector.io.*;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Iterator;
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



    public static long calculateSize(File file) {
        long size = 0;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    size += calculateSize(child);
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

    public static String getImageInfo(File file) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("--------- 图片信息 ---------").append("\n");

        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        sb.append("分辨率: ").append(width).append(" x ").append(height).append("\n");

        ImageMetadata metadata = Imaging.getMetadata(file);
        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            TiffField xResolutionField = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_XRESOLUTION);
            TiffField yResolutionField = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_YRESOLUTION);
            if (xResolutionField != null && yResolutionField != null) {
                RationalNumber xResolution = (RationalNumber) xResolutionField.getValue();
                RationalNumber yResolution = (RationalNumber) yResolutionField.getValue();
                double xDpi = xResolution.doubleValue();
                double yDpi = yResolution.doubleValue();
                sb.append("水平DPI: ").append(xDpi).append("\n");
                sb.append("垂直DPI: ").append(yDpi).append("\n");
            }

            TiffField dateTimeField = jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            if (dateTimeField!= null) {
                String captureTime = dateTimeField.getStringValue();
                sb.append("拍摄时间: ").append(captureTime).append("\n");
            }
           TiffField makeField = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MAKE);
            if (makeField!= null) {
                String make = makeField.getStringValue();
                sb.append("Camera maker: ").append(make).append("\n");
            }
            TiffField modelField = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_MODEL);
            if (modelField!= null) {
                String model = modelField.getStringValue();
                sb.append("Camera model: ").append(model).append("\n");
            }
            TiffField softwareField = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_SOFTWARE);
            if (softwareField!= null) {
                String software = softwareField.getStringValue();
                sb.append("Program name: ").append(software).append("\n");
            }
        }
        return sb.toString();
    }

    public static String getAudioInfo(File file ) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("--------- 音乐信息 ---------").append("\n");
            AudioFile audio = AudioFileIO.read(file);
            long duration = audio.getAudioHeader().getTrackLength();
            sb.append("时长: ").append(duration).append(" 秒\n");
            Tag tag = audio.getTag();
            if (tag != null) {
                String artist = tag.getFirst(FieldKey.ARTIST);
                String title = tag.getFirst(FieldKey.TITLE);
                String album = tag.getFirst(FieldKey.ALBUM);
                String comment = tag.getFirst(FieldKey.COMMENT);
                sb.append("Artist: ").append(artist).append("\n");
                sb.append("Title: ").append(title).append("\n");
                sb.append("Album: ").append(album).append("\n");
                sb.append("Comment: ").append(comment).append("\n");
                return sb.toString();
//                for (FieldKey field : FieldKey.values()) {
//                    String value = tag.getFirst(field);
//                    if (value != null) {
//                        System.out.println(field.name() + ": " + value);
//                    }
//                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }

        return "";
    }

}