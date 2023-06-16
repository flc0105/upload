package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Service
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    private final AppConfig config;

    @Autowired
    public FileController(FileService fileService, AppConfig config) {
        this.fileService = fileService;
        this.config = config;
    }


    @Log
    @PostMapping("/list")
    public Result list(@RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.list(currentDirectory, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/search")
    public Result search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.search(filter, currentDirectory, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/upload")
    public Result upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") Optional<String> currentDirectory, HttpServletRequest request) throws Exception {
        String value = currentDirectory.orElse("/");
        return fileService.upload(files, value, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/mkdir")
    public Result mkdir(@RequestParam("relativePath") String relativePath, HttpServletRequest request) {
        return fileService.mkdir(relativePath, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/delete")
    public Result delete(@RequestParam("relativePath") String files, HttpServletRequest request) throws Exception {
        return fileService.delete(files, CookieUtil.getCookie("token", request));
    }

    @Log
    @RequestMapping("/download")
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.download(relativePath, response);
    }

    @Log
    @RequestMapping("/downloadFolder")
    public void downloadFolder(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.downloadFolder(relativePath, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @RequestMapping("/previewImage")
    public void previewImage(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (config.isCompressImage()) {
            fileService.compress(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    @Log
    @RequestMapping("/zip")
    public Result zip(@RequestParam("relativePath") String relativePath, HttpServletRequest request) throws Exception {
        return new Result<>(fileService.zipFolder(relativePath, CookieUtil.getCookie("token", request)).isFile(), null);
    }

    @Log
    @RequestMapping("/bulkZip")
    public Result bulkZip(@RequestParam("relativePath") String files, HttpServletRequest request) throws Exception {
        return new Result<>(fileService.bulkZip(files, CookieUtil.getCookie("token", request)).isFile(), null);
    }

    @Log
    @RequestMapping("/bulk")
    public void bulk(@RequestParam("relativePath") String files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.bulkDownload(files, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/read")
    public Result read(@RequestParam("relativePath") String relativePath, HttpServletRequest request) throws Exception {
        return fileService.read(relativePath);
    }

    @Log
    @PostMapping("/move")
    public Result move(@RequestParam("src") String src, @RequestParam("dst") String dst, HttpServletRequest request) throws Exception {
        return fileService.move(src, dst, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/rename")
    public Result rename(@RequestParam("src") String src, @RequestParam("dst") String dst, HttpServletRequest request) {
        return fileService.rename(src, dst, CookieUtil.getCookie("token", request));
    }
}