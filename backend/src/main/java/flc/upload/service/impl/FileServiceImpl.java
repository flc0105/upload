package flc.upload.service.impl;

import flc.upload.exception.BusinessException;
import flc.upload.exception.VerifyFailedException;
import flc.upload.manager.TokenManager;
import flc.upload.model.AppConfig;
import flc.upload.model.Folder;
import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.FileUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements FileService {
    private TokenManager tokenManager;

    private AppConfig config;

    @Autowired
    public FileServiceImpl(TokenManager tokenManager, AppConfig config) {
        this.tokenManager = tokenManager;
        this.config = config;
    }

    @Value("${upload.path}")
    private String uploadPath;


    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public Result list(String currentDirectory, String token) {
        List<Folder> folders = new ArrayList<>();
        List<flc.upload.model.File> files = new ArrayList<>();
        if (config.getPrivateDirectories().contains(currentDirectory)) {
            tokenManager.verify(token);
        }
        File[] list = new File(uploadPath, currentDirectory).listFiles();
        if (list == null) {
            return new Result(false, "无法访问");
        }
        for (File file : list) {
            if (file.isDirectory()) {
                folders.add(new Folder(file.getName(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file)));
            }
            if (file.isFile()) {
                files.add(new flc.upload.model.File(file.getName(), file.length(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
            }
        }
        Map<String, List> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        JSONObject jsonObject = JSONObject.fromObject(map);
        return new Result<>(true, null, jsonObject);
    }

    public Result search(String filter, String currentDirectory, String token) {
        List<Folder> folders = new ArrayList<>();
        List<flc.upload.model.File> files = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*" + filter + "*");
        Iterator<Path> iterator = null;
        try {
            try {
                tokenManager.verify(token);
                iterator = Files.walk(Paths.get(uploadPath, currentDirectory)).filter(matcher::matches).iterator();
            } catch (VerifyFailedException e) {
                iterator = Files.walk(Paths.get(uploadPath, currentDirectory))
                        .filter(matcher::matches)
                        .filter(p -> config.getPrivateDirectories().stream().noneMatch(s -> FileUtil.relativize(uploadPath, p.toFile()).startsWith(s)))
                        .iterator();
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        if (iterator == null) {
            return new Result(false, "无法访问");
        }
        while (true) {
            try {
                if (iterator.hasNext()) {
                    Path path = iterator.next();
                    File file = path.toFile();
                    if (Files.isDirectory(path)) {
                        if (Paths.get(uploadPath, currentDirectory).equals(path)) {
                            continue;
                        }
                        folders.add(new Folder(file.getName(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file)));
                    }
                    if (Files.isRegularFile(path)) {
                        files.add(new flc.upload.model.File(file.getName(), file.length(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        Map<String, List> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        JSONObject jsonObject = JSONObject.fromObject(map);
        return new Result<>(true, null, jsonObject);
    }

    public Result upload(MultipartFile[] files, String currentDirectory, String token) throws Exception {
        if (!currentDirectory.startsWith("/public/")) {
            tokenManager.verify(token);
        }
        if (files.length == 0) {
            return new Result(false, "没有文件");
        }
        StringBuilder failedFilenames = new StringBuilder();
        for (MultipartFile file : files) {
            File dest = new File(uploadPath + File.separator + currentDirectory + File.separator + file.getOriginalFilename());
            if (!dest.getParentFile().exists()) {
                logger.info("自动创建目录：{}", dest.getParentFile().mkdirs());
            }
            if (dest.exists()) {
                failedFilenames.append(file.getOriginalFilename()).append(" (已存在)").append(System.lineSeparator());
                continue;
            }
            try {
                file.transferTo(dest);
            } catch (Exception e) {
                failedFilenames.append(file.getOriginalFilename()).append(" (").append(e.getLocalizedMessage()).append(")").append(System.lineSeparator());
            }
        }
        if (failedFilenames.length() == 0) {
            return new Result(true, "上传成功");
        } else {
            return new Result(false, "上传失败的文件：\n" + failedFilenames);
        }
    }

    public Result mkdir(String relativePath, String token) {
        tokenManager.verify(token);
        File directory = new File(uploadPath, relativePath);
        if (directory.exists()) {
            return new Result(false, "文件已存在");
        }
        if (directory.mkdirs()) {
            return new Result(true, "创建成功");
        } else {
            return new Result(false, "创建失败");
        }
    }

    public Result delete(String files, String token) throws Exception {
        tokenManager.verify(token);
        JSONArray array = JSONArray.fromObject(files);
        for (Object file : array) {
            FileUtil.deleteRecursively(new File(uploadPath, file.toString()));
        }
        return new Result(true, "删除成功");
    }

    public void download(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        FileUtil.download(file, response);
    }

    public void compress(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        response.setHeader("Content-type", new MimetypesFileTypeMap().getContentType(file.getName()));
        response.setHeader("Content-Disposition", "attachment;filename=\"" + UriUtils.encode(file.getName(), "UTF-8") + "\"");
        OutputStream out = response.getOutputStream();
        if (file.getName().endsWith(".png")) {
            String jpg = file.getAbsoluteFile() + "_" + new Random().nextInt(1000) + ".jpg";
            Thumbnails.of(file).scale(1f).toFile(jpg);
            Thumbnails.of(jpg).scale(1f).outputQuality(0.5f).toOutputStream(out);
            File f = new File(jpg);
            logger.info("删除{}：{}", jpg, f.delete());
        } else {
            Thumbnails.of(file)
                    .scale(1f)
                    .outputQuality(0.5f)
                    .toOutputStream(out);
        }
    }

    public File zipFolder(String relativePath, String token) throws Exception {
        if (config.getPrivateDirectories().contains(relativePath)) {
            tokenManager.verify(token);
        }
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        String[] pathSplit = relativePath.split("\\\\");
//        String zipName = uploadPath + File.separator + pathSplit[pathSplit.length - 1] + ".zip";
        String zipName = file.getParent() + File.separator + System.currentTimeMillis() + ".zip";
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileUtil.zipFile(zos, file, null);
        zos.close();
        fos.close();
        return new File(zipName);
    }

    public void downloadFolder(String relativePath, HttpServletResponse response, String token) throws Exception {
        // TODO: 压缩失败时返回错误
        File file = zipFolder(relativePath, token);
        if (file.isFile()) {
            FileUtil.download(file, response);
            logger.info("删除压缩文件{}：{}", file.getName(), file.delete());
        } else {
            throw new FileNotFoundException("文件压缩失败");
        }
    }

    public File bulkZip(String files, String token) throws Exception {
        JSONArray array = JSONArray.fromObject(files);
        if (array.isEmpty()) {
            throw new BusinessException("没有可以压缩的文件");
        }
//        String zipName = uploadPath + File.separator + System.currentTimeMillis() + ".zip";
        File firstFile = new File(uploadPath, String.valueOf(array.get(0)));
        String zipName = firstFile.getParentFile() + File.separator + System.currentTimeMillis() + ".zip";
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (Object f : array) {
            if (config.getPrivateDirectories().contains(f.toString())) {
                try {
                    tokenManager.verify(token);
                } catch (VerifyFailedException e) {
                    continue;
                }
            }
            File file = new File(uploadPath, f.toString());
            FileUtil.zipFile(zos, file, null);
        }
        zos.close();
        fos.close();
        return new File(zipName);
    }

    public void bulkDownload(String files, HttpServletResponse response, String token) throws Exception {
        File file = bulkZip(files, token);
        if (file.isFile()) {
            FileUtil.download(file, response);
            logger.info("删除压缩文件{}：{}", file.getName(), file.delete());
        } else {
            throw new FileNotFoundException("文件压缩失败");
        }
    }

    public Result read(String relativePath) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (file.length() > config.getMaxFileSize()) {
            return new Result<>(false, "文件过大，不允许预览");
        }
        String charsetName = FileUtil.getFileEncode(file);
        if (charsetName == null) {
            charsetName = Charset.defaultCharset().name();
        }
        FileInputStream inputStream = new FileInputStream(file);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, charsetName));
        String str;
        StringBuilder sb = new StringBuilder();
        while ((str = in.readLine()) != null) {
            sb.append(str).append("\n");
        }
        in.close();
        return new Result<>(true, null, sb.toString());
    }

    public Result move(String src, String dst, String token) {
        tokenManager.verify(token);
        JSONArray array = JSONArray.fromObject(src);
        StringBuilder failedFilenames = new StringBuilder();
        for (Object file : array) {
            File srcFile = new File(uploadPath + File.separator + file.toString());
            File dstFile = new File(uploadPath + File.separator + dst + File.separator + srcFile.getName());
            if (dstFile.exists()) {
                failedFilenames.append(dstFile.getName()).append(" (已存在)").append(System.lineSeparator());
                continue;
            }
            boolean success = false;
            try {
                success = srcFile.renameTo(dstFile);
            } catch (Exception e) {
                failedFilenames.append(dstFile.getName()).append(" (").append(e.getMessage()).append(")").append(System.lineSeparator());
            }
            if (!success) {
                failedFilenames.append(dstFile.getName()).append(" (移动失败)").append(System.lineSeparator());
            }
        }
        if (failedFilenames.length() == 0) {
            return new Result(true, "移动成功");
        } else {
            return new Result(false, "移动失败的文件：\n" + failedFilenames);
        }
    }

    public Result rename(String oldName, String newName, String token) {
        tokenManager.verify(token);
        File srcFile = new File(uploadPath, oldName);
        File dstFile = new File(uploadPath, newName);
        if (dstFile.exists()) {
            return new Result(false, "文件名已存在");
        }
        try {
            if (srcFile.renameTo(dstFile)) {
                return new Result(true, "重命名成功");
            } else {
                return new Result(false, "重命名失败");
            }
        } catch (Exception e) {
            return new Result(false, e.getMessage());
        }
    }

    @Override
    public Result getFolderInfo(String relativePath) throws Exception {
        File directory = new File(uploadPath, relativePath);
        StringBuilder builder = new StringBuilder();
        if (directory.isDirectory()) {
            long totalSize = FileUtil.calculateSize(directory);
            int fileCount = FileUtil.countFiles(directory);
            int folderCount = FileUtil.countFolders(directory);
            builder.append("文件夹名: ").append(directory.getName()).append("\n");
            builder.append("大小: ").append(FileUtil.formatSize(totalSize)).append("\n");
            builder.append("包含: ").append(fileCount).append("个文件，").append(folderCount).append("个文件夹").append("\n");
            try {
                Path dir = Paths.get(directory.toURI());
                BasicFileAttributes attributes = Files.readAttributes(dir, BasicFileAttributes.class);
                FileTime creationTime = attributes.creationTime();
                FileTime lastModifiedTime = attributes.lastModifiedTime();
                LocalDateTime creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                builder.append("创建时间: ").append(creationDateTime.format(formatter)).append("\n");
                builder.append("修改时间: ").append(lastModifiedDateTime.format(formatter));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else if (directory.isFile()) {
            builder.append("文件名: ").append(directory.getName()).append("\n");
            builder.append("大小: ").append(FileUtil.formatSize(directory.length())).append("\n");
            String fileType = FileUtil.detectFileType(directory);
            builder.append("文件类型: ").append(fileType).append("\n");
            builder.append("相对路径: ").append(FileUtil.relativize(uploadPath, directory)).append("\n");
            Path file = Paths.get(directory.toURI());
            try {
                BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                FileTime creationTime = attributes.creationTime();
                LocalDateTime creationDateTime = LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                FileTime lastModifiedTime = attributes.lastModifiedTime();
                LocalDateTime lastModifiedDateTime = LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
                builder.append("创建时间: ").append(creationDateTime.format(formatter)).append("\n");
                builder.append("修改时间: ").append(lastModifiedDateTime.format(formatter)).append("\n");
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            if (fileType != null) {
                if (fileType.startsWith("image")) {
                    builder.append(FileUtil.getImageInfo(directory));
                }
            }

        }
        return new Result(true, builder.toString());

    }
}
