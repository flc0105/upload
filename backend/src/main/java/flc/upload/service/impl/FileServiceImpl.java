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

    /**
     * Service implementation for uploading files.
     *
     * @param files            Array of MultipartFile representing the files to be uploaded.
     * @param currentDirectory String representing the current directory for upload.
     * @param token            User token for authorization.
     * @return Result containing the outcome of the upload operation.
     * @throws Exception Throws an exception if an error occurs during the upload process.
     */
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
                logger.info("Automatically creating directory {}: {}", dest.getParentFile(), dest.getParentFile().mkdirs());
            }
            if (dest.exists()) {
                dest = FileUtil.getFile(uploadPath, currentDirectory, file.getOriginalFilename() + "_" + System.currentTimeMillis() + FileUtil.getFileExtension(dest));
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

    /**
     * Service implementation for creating a new directory.
     *
     * @param relativePath String representing the relative path for the new directory.
     * @return Result containing the outcome of the mkdir operation.
     */
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

    /**
     * Service implementation for deleting files.
     *
     * @param files List of file paths to be deleted.
     * @return Result containing the outcome of the delete operation.
     * @throws Exception Throws an exception if an error occurs during the deletion process.
     */
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

    /**
     * Service implementation for moving files to a target directory.
     *
     * @param files  List of file paths to be moved.
     * @param target String representing the target directory.
     * @return Result containing the outcome of the move operation.
     */
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

    /**
     * Service implementation for renaming a file.
     *
     * @param relativePath String representing the relative path of the file to be renamed.
     * @param target       String representing the new name for the file.
     * @return Result containing the outcome of the rename operation.
     */
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

    /**
     * Service implementation for listing folders and files in a given directory.
     *
     * @param currentDirectory String representing the current directory.
     * @param token            User token for authorization.
     * @return Result containing a map with folders and files in the specified directory.
     */
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

    /**
     * Service implementation for searching files in a directory based on a filter.
     *
     * @param filter           String representing the filter criteria for file names.
     * @param currentDirectory String representing the current directory.
     * @param token            User token for authorization.
     * @return Result containing a map with folders and files matching the search criteria.
     */
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
            logger.error("Error occurred while searching for files: " + e.getLocalizedMessage());
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
                logger.error("Error occurred while searching for files: " + e.getLocalizedMessage());
            }
        }
        Map<String, List<?>> map = new HashMap<>();
        map.put("folders", folders);
        map.put("files", files);
        return ResponseUtil.buildSuccessResult("query.success", map);
    }

    /**
     * Service implementation for downloading a file.
     *
     * @param relativePath String representing the relative path of the file to be downloaded.
     * @param response     HttpServletResponse to handle the file download response.
     * @throws Exception Throws an exception if an error occurs during the download process.
     */
    @Override
    public void download(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException("file.does.not.exist");
        }
        FileUtil.download(file, response);
    }

    /**
     * Service implementation for downloading a compressed image file.
     *
     * @param relativePath String representing the relative path of the image file to be downloaded.
     * @param response     HttpServletResponse to handle the file download response.
     * @throws Exception Throws an exception if an error occurs during the download process.
     */
    @Override
    public void downloadCompressedImage(String relativePath, HttpServletResponse response) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new BusinessException("file.does.not.exist");
        }
        FileUtil.downloadCompressedImage(file, response);
    }

    /**
     * Service implementation for reading the contents of a text file.
     *
     * @param relativePath String representing the relative path of the text file to be read.
     * @return Result containing the content of the text file.
     * @throws Exception Throws an exception if an error occurs during the read process.
     */
    @Override
    public Result<?> read(String relativePath) throws Exception {
        File file = new File(uploadPath, relativePath);
        if (file.length() > appConfig.getPreviewMaxSize()) {
            return ResponseUtil.buildErrorResult("file.is.too.large");
        }
        String charsetName = Optional.ofNullable(FileUtil.getFileEncode(file)).orElse(Charset.defaultCharset().name());
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.forName(charsetName))) {
            String content = lines.collect(Collectors.joining("\n"));
            return ResponseUtil.buildSuccessResult("query.success", content);
        }
    }

    /**
     * Service implementation for retrieving information about a file.
     *
     * @param relativePath String representing the relative path of the file.
     * @return Result containing details about the file.
     */
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

    /**
     * Service implementation for compressing a list of files into a ZIP archive.
     *
     * @param files List of file paths to be compressed.
     * @param token User token for authorization.
     * @return Result containing the outcome of the compression operation.
     * @throws IOException Throws an exception if an I/O error occurs during the compression process.
     */
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
                        logger.error("Insufficient permissions, skipping private directory: " + relativePath);
                        continue;
                    }
                }
                File file = new File(uploadPath, relativePath);
                FileUtil.zipRecursively(zos, file, null);
            }
        }
        if (zip.isFile()) {
            return ResponseUtil.buildSuccessResult("compress.success", FileUtil.relativize(uploadPath, zip));
        } else {
            return ResponseUtil.buildErrorResult("compress.failure");
        }
    }

    /**
     * Service implementation for compressing a list of files into a ZIP archive and initiating download.
     *
     * @param files    List of file paths to be compressed and downloaded.
     * @param response HttpServletResponse to handle the file download response.
     * @param token    User token for authorization.
     * @throws Exception Throws an exception if an error occurs during the compression or download process.
     */
    @Override
    public void zipAndDownload(List<String> files, HttpServletResponse response, String token) throws Exception {
        Result<?> result = zip(files, token);
        if (result.isSuccess()) {
            File zip = new File(uploadPath, String.valueOf(result.getDetail()));
            if (zip.isFile()) {
                FileUtil.download(zip, response);
                logger.info("Automatically deleting compressed file {}: {}", zip.getAbsoluteFile(), zip.delete());
                return;
            }
        }
        throw new BusinessException("compress.failure");
    }

    /**
     * Service implementation for generating a direct link for an image file.
     *
     * @param relativePath String representing the relative path of the image file.
     * @return Result containing the generated direct link for the image.
     * @throws IOException Throws an exception if an I/O error occurs during the link generation process.
     */
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

    /**
     * Service implementation for retrieving a list of image files in a directory.
     *
     * @param relativePath String representing the relative path of the directory.
     * @return Result containing a list of relative paths to image files.
     */
    @Override
    public Result<?> getImages(String relativePath) {
        List<String> images = new ArrayList<>();
        File dir = new File(uploadPath, relativePath);
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
        for (File file : Objects.requireNonNull(files)) {
            images.add(FileUtil.relativize(uploadPath, file));
        }
        return ResponseUtil.buildSuccessResult("query.success", images);
    }

}
