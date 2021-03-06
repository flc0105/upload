package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.CookieUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/file")
public class FileController {
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/list")
    public Result list(@RequestParam("currentDirectory") String currentDirectory) {
        return fileService.list(currentDirectory);
    }

    @PostMapping("/search")
    public Result search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory) {
        return fileService.search(filter, currentDirectory);
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) throws Exception {
        return fileService.upload(files, currentDirectory, CookieUtil.getCookie("token", request));
    }

    @PostMapping("/mkdir")
    public Result mkdir(@RequestParam("relativePath") String relativePath) {
        return fileService.mkdir(relativePath);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam("relativePath") String files, HttpServletRequest request) throws Exception {
        return fileService.delete(files, CookieUtil.getCookie("token", request));
    }

    @GetMapping("/download")
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        fileService.download(relativePath, response);
    }

    @GetMapping("/zip")
    public void zip(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        fileService.zip(relativePath, response);
    }

    @GetMapping("/bulk")
    public void bulk(@RequestParam("relativePath") String files, HttpServletResponse response) throws Exception {
        fileService.bulk(files, response);
    }

    @PostMapping("/read")
    public Result read(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.read(relativePath);
    }
}