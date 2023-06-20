package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.service.FileService;
import flc.upload.util.CookieUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Service
@RestController
@CrossOrigin(origins = "*")
@Api(tags = "文件")
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
    @ApiOperation("文件_查询所有")
    @PostMapping("/list")
    public Result list(@RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.list(currentDirectory, CookieUtil.getCookie("token", request));
    }

    @Log
    @ApiOperation("文件_搜索")
    @PostMapping("/search")
    public Result search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.search(filter, currentDirectory, CookieUtil.getCookie("token", request));
    }

    @Log
    @ApiOperation("文件_上传")
    @PostMapping("/upload")
    public Result upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") Optional<String> currentDirectory, HttpServletRequest request) throws Exception {
        String value = currentDirectory.orElse("/");
        return fileService.upload(files, value, CookieUtil.getCookie("token", request));
    }

    @Log
    @Token
    @ApiOperation("文件_创建目录")
    @PostMapping("/mkdir")
    public Result mkdir(@RequestParam("relativePath") String relativePath, HttpServletRequest request) {
        return fileService.mkdir(relativePath);
    }

    @Log
    @Token
    @ApiOperation("文件_删除")
    @PostMapping("/delete")
    public Result delete(@RequestParam("relativePath") String files, HttpServletRequest request) throws Exception {
        return fileService.delete(files);
    }

    @Log
    @ApiOperation("文件_下载")
    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.download(relativePath, response);
    }

    @Log
    @ApiOperation("文件_目录打包下载")
    @RequestMapping(value = "/downloadFolder", method = {RequestMethod.GET, RequestMethod.POST})
    public void downloadFolder(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.downloadFolder(relativePath, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @ApiOperation("文件_预览图片")
    @RequestMapping(value = "/previewImage", method = {RequestMethod.GET, RequestMethod.POST})
    public void previewImage(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (config.isCompressImage()) {
            fileService.compress(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    @Log
    @ApiOperation("文件_打包")
    @PostMapping("/zip")
    public Result zip(@RequestParam("relativePath") String relativePath, HttpServletRequest request) throws Exception {
        return new Result<>(fileService.zipFolder(relativePath, CookieUtil.getCookie("token", request)).isFile(), null);
    }

    @Log
    @ApiOperation("文件_批量打包")
    @PostMapping("/bulkZip")
    public Result bulkZip(@RequestParam("relativePath") String files, HttpServletRequest request) throws Exception {
        return new Result<>(fileService.bulkZip(files, CookieUtil.getCookie("token", request)).isFile(), null);
    }

    @Log
    @ApiOperation("文件_预览图片")
    @RequestMapping(value = "/bulk", method = {RequestMethod.GET, RequestMethod.POST})
    public void bulk(@RequestParam("relativePath") String files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.bulkDownload(files, response, CookieUtil.getCookie("token", request));
    }

    @Log
    @ApiOperation("文件_预览文本文件")
    @PostMapping("/read")
    public Result read(@RequestParam("relativePath") String relativePath, HttpServletRequest request) throws Exception {
        return fileService.read(relativePath);
    }

    @Log
    @Token
    @ApiOperation("文件_移动")
    @PostMapping("/move")
    public Result move(@RequestParam("src") String src, @RequestParam("dst") String dst, HttpServletRequest request) throws Exception {
        return fileService.move(src, dst);
    }

    @Log
    @Token
    @ApiOperation("文件_重命名")
    @PostMapping("/rename")
    public Result rename(@RequestParam("src") String src, @RequestParam("dst") String dst, HttpServletRequest request) {
        return fileService.rename(src, dst);
    }

    @Log
    @ApiOperation("文件_查询详情")
    @PostMapping("/info")
    public Result info(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.getFileInfo(relativePath);
    }

    @Log
    @ApiOperation("文件_生成图片直链")
    @PostMapping("/generateDirectLink")
    public Result generateDirectLink(@RequestParam("relativePath") String relativePath) throws IOException {
        return fileService.generateDirectLink(relativePath);
    }
}