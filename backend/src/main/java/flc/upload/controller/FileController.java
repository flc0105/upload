package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
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
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "文件")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/file")
@Service
public class FileController {
    private final FileService fileService;

    private final AppConfig appConfig;

    @Autowired
    public FileController(FileService fileService, AppConfig appConfig) {
        this.fileService = fileService;
        this.appConfig = appConfig;
    }

    @ApiOperation("文件_上传")
    @Log
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") Optional<String> currentDirectory, HttpServletRequest request) throws Exception {
        String value = currentDirectory.orElse("/");
        return fileService.upload(files, value, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("文件_创建目录")
    @Log
    @Permission
    @PostMapping("/mkdir")
    public Result<?> mkdir(@RequestParam("relativePath") String relativePath) {
        return fileService.mkdir(relativePath);
    }

    @ApiOperation("文件_删除")
    @Log
    @Permission
    @PostMapping("/delete")
    public Result<?> delete(@RequestBody Map<String, List<String>> files) throws Exception {
        return fileService.delete(files.get("relativePath"));
    }

    @ApiOperation("文件_移动")
    @Log
    @Token
    @PostMapping("/move")
    public Result<?> move(@RequestBody Map<String, Object> params) throws Exception {
        List<String> relativePath = ((List<?>) params.get("relativePath")).stream().map(String.class::cast).collect(Collectors.toList());
        return fileService.move(relativePath, String.valueOf(params.get("target")));
    }

    @ApiOperation("文件_重命名")
    @Log
    @Permission
    @PostMapping("/rename")
    public Result<?> rename(@RequestBody Map<String, String> params) {
        return fileService.rename(params.get("relativePath"), params.get("target"));
    }

    @ApiOperation("文件_查询所有")
    @Log
    @PostMapping("/list")
    public Result<?> list(@RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.list(currentDirectory, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("文件_搜索")
    @Log
    @PostMapping("/search")
    public Result<?> search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.search(filter, currentDirectory, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("文件_下载")
    @Log
    @Permission
    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        fileService.download(relativePath, response);
    }

    @ApiOperation("文件_预览图片")
    @Log
    @Permission
    @RequestMapping(value = "/preview", method = {RequestMethod.GET, RequestMethod.POST})
    public void preview(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (appConfig.isPreviewCompressImage()) {
            fileService.downloadCompressedImage(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    @ApiOperation("文件_预览文本")
    @Log
    @Permission
    @PostMapping("/read")
    public Result<?> read(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.read(relativePath);
    }

    @ApiOperation("文件_查询详情")
    @Log
    @Permission
    @PostMapping("/info")
    public Result<?> info(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.getFileInfo(relativePath);
    }

    @ApiOperation("文件_压缩")
    @Log
    @Permission
    @PostMapping("/zip")
    public Result<?> zip(@RequestBody Map<String, List<String>> files, HttpServletRequest request) throws Exception {
        return fileService.zip(files.get("relativePath"), CookieUtil.getCookie("token", request));
    }

    @ApiOperation("文件_压缩并下载")
    @Log
    @Permission
    @PostMapping("/zipAndDownload")
    public void zipAndDownload(@RequestBody Map<String, List<String>> files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.zipAndDownload(files.get("relativePath"), response, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("文件_生成图片直链")
    @Log
    @Permission
    @PostMapping("/link")
    public Result<?> link(@RequestParam("relativePath") String relativePath) throws IOException {
        return fileService.generateDirectLink(relativePath);
    }

    @GetMapping("/gallery")
    public Result<?> getImages(@RequestParam("relativePath") String relativePath) {
        return fileService.getImages(relativePath);
    }
}