package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.CookieUtil;
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
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
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
    @RequestMapping("/zip")
    public void zip(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.zip(relativePath, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @GetMapping("/bulk")
    public void bulk(@RequestParam("relativePath") String files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.bulk(files, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/read")
    public Result read(@RequestParam("relativePath") String relativePath) throws Exception {
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