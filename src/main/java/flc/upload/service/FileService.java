package flc.upload.service;

import flc.upload.exception.VerifyFailedException;
import flc.upload.manager.TokenManager;
import flc.upload.model.Folder;
import flc.upload.model.Result;
import flc.upload.util.FileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {
    private TokenManager tokenManager;

    public FileService(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Value("${upload.path}")
    private String uploadPath;

    @Value("#{'${private.directories}'.split(',')}")
    private List<String> privateDirectories;

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    public Result list(String currentDirectory, String token) {
        List<Folder> folders = new ArrayList<>();
        List<flc.upload.model.File> files = new ArrayList<>();
        if (privateDirectories.contains(currentDirectory)) {
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
                        .filter(p -> privateDirectories.stream().noneMatch(s -> FileUtil.relativize(uploadPath, p.toFile()).startsWith(s)))
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
        tokenManager.verify(token);
        if (files.length == 0) {
            return new Result(false, "没有文件");
        }
        StringBuilder failedFilenames = new StringBuilder();
        for (MultipartFile file : files) {
            File dest = new File(uploadPath + File.separator + currentDirectory + File.separator + file.getOriginalFilename());
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
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

    public void zip(String relativePath, HttpServletResponse response, String token) throws Exception {
        if (privateDirectories.contains(relativePath)) {
            tokenManager.verify(token);
        }
        File file = new File(uploadPath, relativePath);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        String[] pathSplit = relativePath.split("\\\\");
        String zipName = uploadPath + File.separator + pathSplit[pathSplit.length - 1] + ".zip";
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileUtil.zipFile(zos, file, null);
        zos.close();
        fos.close();
        File zipFile = new File(zipName);
        FileUtil.download(zipFile, response);
        zipFile.delete();
    }

    public void bulk(String files, HttpServletResponse response, String token) throws Exception {
        JSONArray array = JSONArray.fromObject(files);
        String zipName = uploadPath + File.separator + System.currentTimeMillis() + ".zip";
        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (Object f : array) {
            if (privateDirectories.contains(f.toString())) {
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
        File zipFile = new File(zipName);
        FileUtil.download(zipFile, response);
        zipFile.delete();
    }

    public Result read(String relativePath) throws Exception {
        File file = new File(uploadPath, relativePath);
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
}
