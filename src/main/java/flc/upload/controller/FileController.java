package flc.upload.controller;

import flc.upload.model.Folder;
import flc.upload.model.Result;
import flc.upload.util.FileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipOutputStream;

@Service
@RestController
public class FileController {
    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/list")
    public Result list(@RequestParam("param") String relativePath) {
        File[] files = new File(uploadPath + relativePath).listFiles();
        if (files == null) return new Result("未找到文件", false);
        List<flc.upload.model.File> fileList = new ArrayList<>();
        List<Folder> folderList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (File f : files) {
            if (f.isDirectory()) {
                Folder folder = new Folder();
                folder.setName(f.getName());
                folder.setLastModified(sdf.format(f.lastModified()));
                folder.setRelativePath(FileUtil.relativize(uploadPath, f));
                folderList.add(folder);
            }
            if (f.isFile()) {
                flc.upload.model.File file = new flc.upload.model.File();
                file.setName(f.getName());
                file.setLength(f.length());
                file.setLastModified(sdf.format(f.lastModified()));
                file.setRelativePath(FileUtil.relativize(uploadPath, f));
                try {
                    file.setFileType(new Tika().detect(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileList.add(file);
            }
        }
        Map map = new HashMap();
        map.put("folders", folderList);
        map.put("files", fileList);
        JSONObject jsonObject = JSONObject.fromObject(map);
        return new Result(true, jsonObject);
    }

    @PostMapping("/find")
    public Result find(@RequestParam("param") String filename, @RequestParam("dir") String relativePath) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/*" + filename + "*");
        Collection<Path> find;
        try {
            find = FileUtil.find(uploadPath + relativePath, matcher);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(e.getMessage(), false);
        }
        if (find.isEmpty()) {
            return new Result("没有结果", false);
        }
        List<flc.upload.model.File> files = new ArrayList<>();
        List<Folder> folders = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Iterator<Path> it = find.iterator();
        while (it.hasNext()) {
            Path path = it.next();
            File f = path.toFile();
            if (Files.isRegularFile(path)) {
                flc.upload.model.File file = new flc.upload.model.File();
                file.setName(f.getName());
                file.setLength(f.length());
                file.setLastModified(sdf.format(f.lastModified()));
                file.setRelativePath(FileUtil.relativize(uploadPath, f));
                try {
                    file.setFileType(new Tika().detect(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                files.add(file);
            }
            if (Files.isDirectory(path)) {
                if (Paths.get(uploadPath + relativePath).equals(path)) {
                    continue;
                }
                Folder folder = new Folder();
                folder.setName(f.getName());
                folder.setLastModified(sdf.format(f.lastModified()));
                folder.setRelativePath(FileUtil.relativize(uploadPath, f));
                folders.add(folder);
            }
        }
        Map map = new HashMap();
        map.put("folders", folders);
        map.put("files", files);
        JSONObject jsonObject = JSONObject.fromObject(map);
        return new Result(true, jsonObject);
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile[] files, @RequestParam("dir") String dir) throws Exception {
        StringBuilder fn = new StringBuilder();
        for (MultipartFile file : files) {
            File dest = new File(uploadPath + File.separator + dir + File.separator + file.getOriginalFilename());
            if (dest.exists()) {
                fn.append(file.getOriginalFilename()).append("<br>");
                continue;
            }
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        }
        if (fn.length() == 0) {
            return new Result(true);
        } else {
            return new Result("以下文件已存在，未成功上传<br>" + fn, false);
        }
    }

    @PostMapping("/uploadFolder")
    public Result uploadFolder(@RequestParam("folder") MultipartFile[] files, @RequestParam("dir") String dir) throws Exception {
        StringBuilder fn = new StringBuilder();
        for (MultipartFile file : files) {
            if (Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
                return new Result("不能上传空文件夹", false);
            }
            File dest = new File(uploadPath + File.separator + dir + File.separator + file.getOriginalFilename());
            if (dest.exists()) {
                fn.append(file.getOriginalFilename()).append("<br>");
                continue;
            }
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        }
        if (fn.length() == 0) {
            return new Result(true);
        } else {
            return new Result("以下文件已存在，未成功上传<br>" + fn, false);
        }
    }

    @PostMapping("/createFolder")
    public Result createFolder(@RequestParam("folderName") String folderName, @RequestParam("dir") String dir) {
        File folder = new File(uploadPath + dir + File.separator + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
            return new Result(true);
        } else {
            return new Result("文件夹已存在", false);
        }
    }

    @RequestMapping("/download")
    public void download(HttpServletResponse response, @RequestParam("fn") String fn) throws IOException {
        File file = new File(uploadPath + File.separator + fn);
        if (!file.exists()) {
            return;
        }
        FileUtil.downloadFile(response, file);
    }

    @RequestMapping("/createZip")
    public void createZip(HttpServletResponse response, @RequestParam("dir") String dir) throws IOException {
        String[] ds = dir.split("\\\\");
        String fn = ds[ds.length - 1];
        FileOutputStream fos = new FileOutputStream(uploadPath + fn + ".zip");
        ZipOutputStream zos = new ZipOutputStream(fos);
        try {
            FileUtil.createZip(zos, new File(uploadPath + dir), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        zos.flush();
        fos.flush();
        zos.close();
        fos.close();
        File file = new File(uploadPath + fn + ".zip");
        FileUtil.downloadFile(response, file);
        file.delete();
    }

    @RequestMapping("/multiDownload")
    public void multiDownload(HttpServletResponse response, @RequestParam("files") String files) throws IOException {
        JSONArray array = JSONArray.fromObject(files);
        String zipName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        FileOutputStream fos = new FileOutputStream(uploadPath + File.separator + zipName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (Object f : array) {
            File file = new File(uploadPath + File.separator + f.toString());
            try {
                FileUtil.createZip(zos, file, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zos.flush();
        fos.flush();
        zos.close();
        fos.close();
        File file = new File(uploadPath + File.separator + zipName);
        FileUtil.downloadFile(response, file);
        file.delete();
    }

    @PostMapping("/delete")
    public void delete(@RequestParam("fn") String fn) {
        File file = new File(uploadPath + File.separator + fn);
        FileUtil.deleteFolder(file);
    }

    @PostMapping("/multiDelete")
    public void multiDelete(@RequestParam("files") String files) {
        JSONArray array = JSONArray.fromObject(files);
        for (Object f : array) {
            File file = new File(uploadPath + File.separator + f.toString());
            FileUtil.deleteFolder(file);
        }
    }

    @PostMapping("/rename")
    public Result rename(@RequestParam("fn") String fn, @RequestParam("newname") String newname) {
        File file = new File(uploadPath + File.separator + fn);
        File newFile = new File(uploadPath + File.separator + newname);
        if (newFile.exists()) {
            return new Result("文件已存在", false);
        }
        if (file.renameTo(newFile)) {
            return new Result(true);
        }
        return new Result(false);
    }

    @PostMapping("/move")
    public Result move(@RequestParam("fn") String fn, @RequestParam("dir") String dir) {
        JSONArray array = JSONArray.fromObject(fn);
        StringBuilder files = new StringBuilder();
        for (Object f : array) {
            File file = new File(uploadPath + File.separator + f.toString());
            File newFile = new File(uploadPath + File.separator + dir + File.separator + file.getName());
            if (newFile.exists()) {
                files.append(f.toString()).append("<br>");
                continue;
            }
            file.renameTo(newFile);
        }
        if (files.length() == 0) {
            return new Result(true);
        } else {
            return new Result("以下文件已存在，未成功移动<br>" + files, false);
        }
    }

    @RequestMapping("/previewImage")
    public byte[] previewImage(@RequestParam("fn") String fn) throws IOException {
        File file = new File(uploadPath + File.separator + fn);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        inputStream.close();
        return bytes;
    }

    @PostMapping("/previewText")
    public String previewText(@RequestParam("fn") String fn) throws IOException {
        File file = new File(uploadPath + File.separator + fn);
        String charsetName = FileUtil.getFileEncode(file);
        FileInputStream inputStream = new FileInputStream(file);
        assert charsetName != null;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, charsetName));
        String str;
        StringBuilder sb = new StringBuilder();
        while ((str = in.readLine()) != null) {
            sb.append(str).append("\n");
        }
        in.close();
        return sb.toString();
    }
}