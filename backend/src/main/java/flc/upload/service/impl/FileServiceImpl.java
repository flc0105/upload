package flc.upload.service.impl;

import flc.upload.exception.BusinessException;
import flc.upload.exception.VerifyFailedException;
import flc.upload.manager.TokenManager;
import flc.upload.model.AppConfig;
import flc.upload.model.Folder;
import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.CommonUtil;
import flc.upload.util.FileUtil;
import flc.upload.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements FileService {
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final TokenManager tokenManager;

    private final AppConfig appConfig;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public FileServiceImpl(TokenManager tokenManager, AppConfig appConfig) {
        this.tokenManager = tokenManager;
        this.appConfig = appConfig;
    }

    @Override
    public Result<?> list(String currentDirectory, String token) {
        List<Folder> folders = new ArrayList<>();
        List<flc.upload.model.File> files = new ArrayList<>();
        if (appConfig.getPrivateDirectories().contains(currentDirectory)) {
            tokenManager.verify(token);
        }
        File[] list = new File(uploadPath, currentDirectory).listFiles();
        if (list == null) {
            return ResponseUtil.buildErrorResult("access.failure");
        }
        for (File file : list) {
            if (file.isDirectory()) {
                folders.add(new Folder(file.getName(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file)));
            }
            if (file.isFile()) {
                files.add(new flc.upload.model.File(file.getName(), file.length(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
            }
        }
        Map<String, List<?>> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        return ResponseUtil.buildSuccessResult("query.success", map);
    }

    @Override
    public Result<?> search(String filter, String currentDirectory, String token) {
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
                        .filter(p -> appConfig.getPrivateDirectories().stream().noneMatch(s -> FileUtil.relativize(uploadPath, p.toFile()).startsWith(s)))
                        .iterator();
            }
        } catch (IOException e) {
            logger.error("搜索文件时出错：" + e.getLocalizedMessage());
        }
        if (iterator == null) {
            return ResponseUtil.buildErrorResult("access.failure");
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
                logger.error("搜索文件时出错：" + e.getLocalizedMessage());
            }
        }
        Map<String, List<?>> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        return ResponseUtil.buildSuccessResult("query.success", map);
    }

    @Override
    public Result<?> upload(MultipartFile[] files, String currentDirectory, String token) throws Exception {
        if (!currentDirectory.startsWith("/public/")) {
            tokenManager.verify(token);
        }
        if (files.length == 0) {
            return ResponseUtil.buildErrorResult("no.incoming.files");
        }
        List<String> failures = new ArrayList<>();
        for (MultipartFile file : files) {
            File dest = FileUtil.getFile(uploadPath, currentDirectory, file.getOriginalFilename());
            if (!dest.getParentFile().exists()) {
                logger.info("自动创建目录 {}：{}", dest.getParentFile(), dest.getParentFile().mkdirs());
            }
            if (dest.exists()) {
                failures.add(file.getOriginalFilename() + " (" + ResponseUtil.getMessage("file.already.exists") + ")");
                continue;
            }
            try {
                file.transferTo(dest);
            } catch (Exception e) {
                failures.add(file.getOriginalFilename() + " (" + e.getLocalizedMessage() + ")");
            }
        }
        if (failures.isEmpty()) {
            return ResponseUtil.buildSuccessResult("upload.success");
        } else {
            return new Result<>(false, ResponseUtil.getMessage("some.files.upload.failure") + System.lineSeparator() + String.join(System.lineSeparator(), failures));
        }
    }

    @Override
    public Result<?> mkdir(String relativePath) {
        File directory = new File(uploadPath, relativePath);
        if (directory.exists()) {
            return ResponseUtil.buildErrorResult("file.already.exists");
        }
        if (directory.mkdirs()) {
            return ResponseUtil.buildSuccessResult("create.directory.success");
        } else {
            return ResponseUtil.buildErrorResult("create.directory.failure");
        }
    }

    @Override
    public Result<?> delete(List<String> files) throws Exception {
        if (files.isEmpty()) {
            return ResponseUtil.buildErrorResult("no.incoming.files");
        }
        for (String file : files) {
            FileUtil.deleteRecursively(new File(uploadPath, file));
        }
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @Override
    public void download(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException(ResponseUtil.getMessage("file.does.not.exist"));
        }
        FileUtil.download(file, response);
    }

    @Override
    public void downloadCompressedImage(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException(ResponseUtil.getMessage("file.does.not.exist"));
        }
        FileUtil.downloadCompressedImage(file, response);
    }

    @Override
    public Result<?> read(String relativePath) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (file.length() > appConfig.getMaxFileSize()) {
            return ResponseUtil.buildErrorResult("file.is.too.large");
        }
        String charsetName = Optional.ofNullable(FileUtil.getFileEncode(file)).orElse(Charset.defaultCharset().name());
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.forName(charsetName))) {
            String content = lines.collect(Collectors.joining("\n"));
            return ResponseUtil.buildSuccessResult("query.success", content);
        }
    }

    @Override
    public Result move(String src, String dst) {
        return null;
    }

    @Override
    public Result<?> zip(List<String> files, String token) throws IOException {
        if (files.isEmpty()) {
            return ResponseUtil.buildErrorResult("no.incoming.files");
        }
        File firstFile = new File(uploadPath, files.get(0));
        File zip = FileUtil.getFile(firstFile.getParent(), CommonUtil.generateUUID() + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zip);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String relativePath : files) {
                if (appConfig.getPrivateDirectories().contains(relativePath)) {
                    try {
                        tokenManager.verify(token);
                    } catch (VerifyFailedException e) {
                        logger.error("权限不足，跳过私密目录：" + relativePath);
                        continue;
                    }
                }
                File file = new File(uploadPath, relativePath);
                FileUtil.zipRecursively(zos, file, null);
            }
        }
//        File zip = new File(zipName);
        if (zip.isFile()) {
            return ResponseUtil.buildSuccessResult("compress.success", FileUtil.relativize(uploadPath, zip));
        } else {
            return ResponseUtil.buildErrorResult("compress.failure");
        }
    }

    @Override
    public void zipAndDownload(List<String> files, HttpServletResponse response, String token) throws Exception {
        Result<?> result = zip(files, token);
        if (result.isSuccess()) {
            File zip = new File(uploadPath, String.valueOf(result.getDetail()));
            if (zip.isFile()) {
                FileUtil.download(zip, response);
                logger.info("自动删除压缩文件 {}: {}", zip.getAbsoluteFile(), zip.delete());
                return;
            }
        }
        throw new BusinessException(ResponseUtil.getMessage("compress.failure"));
    }

    @Override
    public Result<?> move(List<String> files, String target) {
        if (files.isEmpty()) {
            return ResponseUtil.buildErrorResult("no.incoming.files");
        }
        List<String> failures = new ArrayList<>();
        for (String relativePath : files) {
            File file = new File(uploadPath, relativePath);
            File targetFile = FileUtil.getFile(uploadPath, target, file.getName());
            try {
                if (targetFile.exists()) {
                    throw new BusinessException(ResponseUtil.getMessage("file.already.exists"));
                }
                if (!file.renameTo(targetFile)) {
                    throw new BusinessException(ResponseUtil.getMessage("move.failure"));
                }
            } catch (Exception e) {
                failures.add(file.getName() + " (" + e.getLocalizedMessage() + ")");
            }
        }
        if (failures.isEmpty()) {
            return ResponseUtil.buildSuccessResult("move.success");
        } else {
            return new Result<>(false, ResponseUtil.getMessage("some.files.move.failure") + System.lineSeparator() + String.join(System.lineSeparator(), failures));
        }
    }

    public Result<?> rename(String relativePath, String target) {
        File file = new File(uploadPath, relativePath);
        File targetFile = new File(uploadPath, target);

        try {
            if (targetFile.exists()) {
                throw new BusinessException(ResponseUtil.getMessage("file.already.exists"));
            }
            if (!file.renameTo(targetFile)) {
                throw new BusinessException(ResponseUtil.getMessage("rename.failure"));
            }
        } catch (Exception e) {
            return new Result<>(false, e.getLocalizedMessage());
        }
        return ResponseUtil.buildSuccessResult("rename.success");
    }

    @Override
    public Result<?> generateDirectLink(String relativePath) throws IOException {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            return ResponseUtil.buildErrorResult("file.does.not.exist");
        }
        if (file.isFile() && FileUtil.detectFileType(file).startsWith("image")) {
            String target = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + FileUtil.getFileExtension(file);
            Path targetFile = Paths.get(uploadPath, "images", target);
            Files.createDirectories(targetFile.getParent());
            Files.copy(Paths.get(file.toURI()), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return ResponseUtil.buildSuccessResult("generate.success", "/images/" + target);
        }
        return ResponseUtil.buildErrorResult("unsupported.file.type");
    }


    @Override
    public Result getFileInfo(String relativePath) throws Exception {
        File f = new File(uploadPath, relativePath);
        StringBuilder sb = new StringBuilder();
        if (f.isDirectory()) {
            sb.append("--------- 文件夹属性 ---------").append("\n");
            sb.append("文件夹名: ").append(f.getName()).append("\n");
            sb.append("大小: ").append(FileUtil.formatSize(FileUtil.calculateDirectorySize(f))).append("\n");
            sb.append("包含: ").append(FileUtil.countFiles(f)).append("个文件，").append(FileUtil.countFolders(f)).append("个文件夹").append("\n");
            sb.append("相对路径: ").append(FileUtil.relativize(uploadPath, f)).append("\n");
            sb.append("创建时间: ").append(FileUtil.getCreationTime(f)).append("\n");
            sb.append("修改时间: ").append(FileUtil.getModifiedTime(f)).append("\n");
        } else if (f.isFile()) {
            sb.append("--------- 文件属性 ---------").append("\n");
            sb.append("文件名: ").append(f.getName()).append("\n");
            sb.append("大小: ").append(FileUtil.formatSize(f.length())).append("\n");
            sb.append("相对路径: ").append(FileUtil.relativize(uploadPath, f)).append("\n");
            String fileType = FileUtil.detectFileType(f);
            sb.append("文件类型: ").append(fileType).append("\n");
            sb.append("创建时间: ").append(FileUtil.getCreationTime(f)).append("\n");
            sb.append("修改时间: ").append(FileUtil.getModifiedTime(f)).append("\n");
            if (fileType != null) {
                if (fileType.startsWith("image")) {
                    sb.append(FileUtil.getImageInfo(f));
                } else if (fileType.startsWith("audio")) {
                    sb.append(FileUtil.getAudioInfo(f));
                }
            }
        }
        return new Result(true, "查询成功", sb.toString());
    }


}
