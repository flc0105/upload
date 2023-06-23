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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @GetMapping("/list")
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
    public Result delete(@RequestBody Map<String, List<String>> files, HttpServletRequest request) throws Exception {
        return fileService.delete(files.get("relativePath"));
    }

    @Log
    @ApiOperation("文件_下载")
    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.download(relativePath, response);
    }


    @Log
    @ApiOperation("文件_预览图片")
    @RequestMapping(value = "/previewImage", method = {RequestMethod.GET, RequestMethod.POST})
    public void previewImage(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (config.isCompressImage()) {
            fileService.downloadCompressedImage(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    @Log
    @ApiOperation("文件_压缩")
    @PostMapping("/zip")
    public Result<?> zip(@RequestBody Map<String, List<String>> files, HttpServletRequest request) throws Exception {
        return fileService.zip(files.get("relativePath"), CookieUtil.getCookie("token", request));
    }

    @Log
    @ApiOperation("文件_压缩并下载")
    @PostMapping("/zipAndDownload")
    public void zipAndDownload(@RequestBody Map<String, List<String>> files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.zipAndDownload(files.get("relativePath"), response, CookieUtil.getCookie("token", request));
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
    public Result<?> move(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
        List<String> relativePath = ((List<?>) params.get("relativePath")).stream().map(String.class::cast).collect(Collectors.toList());
        return fileService.move(relativePath, String.valueOf(params.get("target")));
    }

    @Log
    @Token
    @ApiOperation("文件_重命名")
    @PostMapping("/rename")
    public Result rename(@RequestBody Map<String, String> params, HttpServletRequest request) {
        return fileService.rename(params.get("relativePath"), params.get("target"));
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