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
import flc.upload.util.InternationalizationUtil;
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
                failures.add(file.getOriginalFilename() + " (" + InternationalizationUtil.translate("file.already.exists") + ")");
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
            return new Result<>(false, InternationalizationUtil.translate("some.files.upload.failure") + System.lineSeparator() + String.join(System.lineSeparator(), failures));
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
            if (file.trim().isEmpty()) {
                continue;
            }
            FileUtil.deleteRecursively(new File(uploadPath, file));
        }
        return ResponseUtil.buildSuccessResult("delete.success");
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
                    throw new BusinessException("file.already.exists");
                }
                if (!file.renameTo(targetFile)) {
                    throw new BusinessException("move.failure");
                }
            } catch (Exception e) {
                failures.add(file.getName() + " (" + e.getLocalizedMessage() + ")");
            }
        }
        if (failures.isEmpty()) {
            return ResponseUtil.buildSuccessResult("move.success");
        } else {
            return new Result<>(false, InternationalizationUtil.translate("some.files.move.failure") + System.lineSeparator() + String.join(System.lineSeparator(), failures));
        }
    }

    @Override
    public Result<?> rename(String relativePath, String target) {
        File file = new File(uploadPath, relativePath);
        File targetFile = new File(uploadPath, target);
        try {
            if (targetFile.exists()) {
                throw new BusinessException("file.already.exists");
            }
            if (!file.renameTo(targetFile)) {
                throw new BusinessException("rename.failure");
            }
        } catch (Exception e) {
            return new Result<>(false, e.getLocalizedMessage());
        }
        return ResponseUtil.buildSuccessResult("rename.success");
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
                folders.add(new Folder(file.getName(), CommonUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file)));
            }
            if (file.isFile()) {
                files.add(new flc.upload.model.File(file.getName(), file.length(), CommonUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
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
                iterator = Files.walk(Paths.get(uploadPath, currentDirectory)).filter(matcher::matches).filter(p -> appConfig.getPrivateDirectories().stream().noneMatch(s -> FileUtil.relativize(uploadPath, p.toFile()).startsWith(s))).iterator();
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
                        folders.add(new Folder(file.getName(), CommonUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file)));
                    }
                    if (Files.isRegularFile(path)) {
                        files.add(new flc.upload.model.File(file.getName(), file.length(), CommonUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
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
    public void download(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException("file.does.not.exist");
        }
        FileUtil.download(file, response);
    }

    @Override
    public void downloadCompressedImage(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException("file.does.not.exist");
        }
        FileUtil.downloadCompressedImage(file, response);
    }

    @Override
    public Result<?> read(String relativePath) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (file.length() > appConfig.getPreviewMaxFileSize()) {
            return ResponseUtil.buildErrorResult("file.is.too.large");
        }
        String charsetName = Optional.ofNullable(FileUtil.getFileEncode(file)).orElse(Charset.defaultCharset().name());
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.forName(charsetName))) {
            String content = lines.collect(Collectors.joining("\n"));
            return ResponseUtil.buildSuccessResult("query.success", content);
        }
    }

    @Override
    public Result<?> getFileInfo(String relativePath) {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException("file.does.not.exist");
        }
        Map<String, String> map = new LinkedHashMap<>();
        map.put("filename", file.getName());
        map.put("relative.path", FileUtil.relativize(uploadPath, file));
        map.put("creation.time", FileUtil.getCreationTime(file));
        map.put("modified.time", FileUtil.getModifiedTime(file));
        if (file.isDirectory()) {
            map.put("length", FileUtil.formatSize(FileUtil.calculateDirectorySize(file)));
            map.put("contains", InternationalizationUtil.translate("file.folder.count", FileUtil.countFiles(file), FileUtil.countFolders(file)));
        } else if (file.isFile()) {
            map.put("length", FileUtil.formatSize(file.length()));
            map.put("file.type", FileUtil.detectFileType(file));
            map.put("file.hash", FileUtil.calculateMD5(file.getAbsolutePath()));
            if (FileUtil.isImage(file)) {
                map.putAll(FileUtil.getImageInfo(file));
            } else if (FileUtil.isAudio(file)) {
                map.putAll(FileUtil.getAudioInfo(file));
            }
        }
        String result = InternationalizationUtil.translateMapKeys(map).entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining(System.lineSeparator()));
        return ResponseUtil.buildSuccessResult("query.success", result);
    }

    @Override
    public Result<?> zip(List<String> files, String token) throws IOException {
        if (files.isEmpty()) {
            return ResponseUtil.buildErrorResult("no.incoming.files");
        }
        File firstFile = new File(uploadPath, files.get(0));
        File zip = FileUtil.getFile(firstFile.getParent(), CommonUtil.generateUUID() + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zip); ZipOutputStream zos = new ZipOutputStream(fos)) {
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
        throw new BusinessException("compress.failure");
    }

    @Override
    public Result<?> generateDirectLink(String relativePath) throws IOException {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            return ResponseUtil.buildErrorResult("file.does.not.exist");
        }
        if (file.isFile() && FileUtil.isImage(file)) {
            String target = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + FileUtil.getFileExtension(file);
            Path targetFile = Paths.get(uploadPath, "images", target);
            Files.createDirectories(targetFile.getParent());
            Files.copy(Paths.get(file.toURI()), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return ResponseUtil.buildSuccessResult("generate.success", "/images/" + target);
        }
        return ResponseUtil.buildErrorResult("unsupported.file.type");
    }

    @Override
    public Result<?> getImages(String relativePath) {
        List<String> images = new ArrayList<>();

        // 根据目录参数获取该目录中的所有图片文件

        // 假设使用java.io.File类获取目录中的图片文件
        File dir = new File(uploadPath, relativePath);
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        // 将图片文件的URL添加到列表中
        for (File file : Objects.requireNonNull(files)) {
            images.add(FileUtil.relativize(uploadPath, file));
        }

        return ResponseUtil.buildSuccessResult("query.success", images);
    }



}
