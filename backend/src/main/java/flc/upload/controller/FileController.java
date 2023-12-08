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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @ApiOperation("上传文件")
    @Log
    @Permission // 即使允许的状态下也只能在/public目录上传
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") Optional<String> currentDirectory, HttpServletRequest request) throws Exception {
        String value = currentDirectory.orElse("/");
        return fileService.upload(files, value, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("创建目录")
    @Log
    @Permission
    @PostMapping("/mkdir")
    public Result<?> mkdir(@RequestParam("relativePath") String relativePath) {
        return fileService.mkdir(relativePath);
    }

    @ApiOperation("删除文件")
    @Log
    @Permission
    @PostMapping("/delete")
    public Result<?> delete(@RequestBody Map<String, List<String>> files) throws Exception {
        return fileService.delete(files.get("relativePath"));
    }

    @ApiOperation("移动文件")
    @Log
    @Permission
    @PostMapping("/move")
    public Result<?> move(@RequestBody Map<String, Object> params) throws Exception {
        List<String> relativePath = ((List<?>) params.get("relativePath")).stream().map(String.class::cast).collect(Collectors.toList());
        return fileService.move(relativePath, String.valueOf(params.get("target")));
    }

    @ApiOperation("重命名文件")
    @Log
    @Permission
    @PostMapping("/rename")
    public Result<?> rename(@RequestBody Map<String, String> params) {
        return fileService.rename(params.get("relativePath"), params.get("target"));
    }

    @ApiOperation("获取文件列表")
    @Permission // 即使允许的情况下也不可以获取私密目录下的文件
    @PostMapping("/list")
    public Result<?> list(@RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.list(currentDirectory, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("搜索文件")
    @Log
    @Permission // 即使允许的情况下也搜索不到私密目录下的文件
    @PostMapping("/search")
    public Result<?> search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.search(filter, currentDirectory, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("下载文件")
    @Log
    @Permission
    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        fileService.download(relativePath, response);
    }

    @ApiOperation("预览图片")
    @Log
    @Permission
    @RequestMapping(value = "/preview", method = {RequestMethod.GET, RequestMethod.POST})
    public void preview(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (appConfig.isFilePreviewCompressImage()) {
            fileService.downloadCompressedImage(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    @ApiOperation("预览文本文件")
    @Log
    @Permission
    @PostMapping("/read")
    public Result<?> read(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.read(relativePath);
    }

    @ApiOperation("获取文件详情")
    @Log
    @Permission
    @PostMapping("/info")
    public Result<?> info(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.getFileInfo(relativePath);
    }

    @ApiOperation("压缩文件")
    @Log
    @Permission // 即使允许的情况下也会跳过私密目录下的文件
    @PostMapping("/zip")
    public Result<?> zip(@RequestBody Map<String, List<String>> files, HttpServletRequest request) throws Exception {
        return fileService.zip(files.get("relativePath"), CookieUtil.getCookie("token", request));
    }

    @ApiOperation("压缩并下载文件")
    @Log
    @Permission // 即使允许的情况下也会跳过私密目录下的文件
    @PostMapping("/zipAndDownload")
    public void zipAndDownload(@RequestBody Map<String, List<String>> files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.zipAndDownload(files.get("relativePath"), response, CookieUtil.getCookie("token", request));
    }

    @ApiOperation("生成图片直链")
    @Log
    @Permission
    @PostMapping("/link")
    public Result<?> link(@RequestParam("relativePath") String relativePath) throws IOException {
        return fileService.generateDirectLink(relativePath);
    }

    @ApiOperation("获取图片列表")
    @Log
    @Permission // TODO: 允许的情况下可以获取到私密目录的图片
    @GetMapping("/gallery")
    public Result<?> getImages(@RequestParam("relativePath") String relativePath) {
        return fileService.getImages(relativePath);
    }
}