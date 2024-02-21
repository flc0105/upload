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

@Api(tags = "File")
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

    /**
     * Endpoint to handle file upload.
     *
     * @param files             Array of MultipartFile representing the files to be uploaded.
     * @param currentDirectory  Optional parameter representing the current directory for upload.
     * @param request           HttpServletRequest to retrieve the user's token.
     * @return                  Result containing the outcome of the upload operation.
     * @throws Exception        Throws an exception if an error occurs during the upload process.
     */
    @ApiOperation("Upload files")
    @Log
    @Permission // Even if allowed, uploading is only allowed in the /public directory
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("files") MultipartFile[] files, @RequestParam("currentDirectory") Optional<String> currentDirectory, HttpServletRequest request) throws Exception {
        String value = currentDirectory.orElse("/");
        return fileService.upload(files, value, CookieUtil.getCookie("token", request));
    }

    /**
     * Endpoint to create a new directory.
     *
     * @param relativePath  String representing the relative path for the new directory.
     * @return              Result containing the outcome of the mkdir operation.
     */
    @ApiOperation("Create directory")
    @Log
    @Permission
    @PostMapping("/mkdir")
    public Result<?> mkdir(@RequestParam("relativePath") String relativePath) {
        return fileService.mkdir(relativePath);
    }

    /**
     * Endpoint to delete files.
     *
     * @param files  Map containing a list of relative paths to files for deletion.
     * @return       Result containing the outcome of the delete operation.
     * @throws Exception Throws an exception if an error occurs during the deletion process.
     */
    @ApiOperation("Delete files")
    @Log
    @Permission
    @PostMapping("/delete")
    public Result<?> delete(@RequestBody Map<String, List<String>> files) throws Exception {
        return fileService.delete(files.get("relativePath"));
    }

    /**
     * Endpoint to move files.
     *
     * @param params Map containing 'relativePath' (List<String>) and 'target' (String) for move operation.
     * @return       Result containing the outcome of the move operation.
     * @throws Exception Throws an exception if an error occurs during the move process.
     */
    @ApiOperation("Move files")
    @Log
    @Permission
    @PostMapping("/move")
    public Result<?> move(@RequestBody Map<String, Object> params) throws Exception {
        List<String> relativePath = ((List<?>) params.get("relativePath")).stream().map(String.class::cast).collect(Collectors.toList());
        return fileService.move(relativePath, String.valueOf(params.get("target")));
    }

    /**
     * Endpoint to rename a file.
     *
     * @param params Map containing 'relativePath' (String) and 'target' (String) for rename operation.
     * @return       Result containing the outcome of the rename operation.
     */
    @ApiOperation("Rename file")
    @Log
    @Permission
    @PostMapping("/rename")
    public Result<?> rename(@RequestBody Map<String, String> params) {
        return fileService.rename(params.get("relativePath"), params.get("target"));
    }

    /**
     * Endpoint to get a list of files in a directory.
     *
     * @param currentDirectory  String representing the current directory.
     * @param request           HttpServletRequest to retrieve the user's token.
     * @return                  Result containing the list of files in the specified directory.
     */
    @ApiOperation("Get file list")
    @Permission // Even if allowed, cannot retrieve files from private directories
    @PostMapping("/list")
    public Result<?> list(@RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.list(currentDirectory, CookieUtil.getCookie("token", request));
    }

    /**
     * Endpoint to search for files.
     *
     * @param filter            String representing the filter for file search.
     * @param currentDirectory  String representing the current directory for the search.
     * @param request           HttpServletRequest to retrieve the user's token.
     * @return                  Result containing the list of files matching the search criteria.
     */
    @ApiOperation("Search for files")
    @Log
    @Permission // Even if allowed, cannot search files in private directories
    @PostMapping("/search")
    public Result<?> search(@RequestParam("filter") String filter, @RequestParam("currentDirectory") String currentDirectory, HttpServletRequest request) {
        return fileService.search(filter, currentDirectory, CookieUtil.getCookie("token", request));
    }

    /**
     * Endpoint to download a file.
     *
     * @param relativePath  String representing the relative path of the file to be downloaded.
     * @param response      HttpServletResponse to handle the file download.
     * @throws Exception    Throws an exception if an error occurs during the download process.
     */
    @ApiOperation("Download file")
    @Log
    @Permission
    @RequestMapping(value = "/download", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        fileService.download(relativePath, response);
    }

    /**
     * Endpoint to preview an image.
     *
     * @param relativePath  String representing the relative path of the image to be previewed.
     * @param response      HttpServletResponse to handle the image preview.
     * @throws Exception    Throws an exception if an error occurs during the preview process.
     */
    @ApiOperation("Preview image")
    @Log
    @Permission
    @RequestMapping(value = "/preview", method = {RequestMethod.GET, RequestMethod.POST})
    public void preview(@RequestParam("relativePath") String relativePath, HttpServletResponse response) throws Exception {
        if (appConfig.isPreviewImageCompress()) {
            fileService.downloadCompressedImage(relativePath, response);
        } else {
            fileService.download(relativePath, response);
        }
    }

    /**
     * Endpoint to read the content of a text file.
     *
     * @param relativePath  String representing the relative path of the text file.
     * @return              Result containing the content of the text file.
     * @throws Exception    Throws an exception if an error occurs during the reading process.
     */
    @ApiOperation("Read text file")
    @Log
    @Permission
    @PostMapping("/read")
    public Result<?> read(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.read(relativePath);
    }

    /**
     * Endpoint to get details about a file.
     *
     * @param relativePath  String representing the relative path of the file.
     * @return              Result containing the details of the file.
     * @throws Exception    Throws an exception if an error occurs during the process.
     */
    @ApiOperation("Get file details")
    @Log
    @Permission
    @PostMapping("/info")
    public Result<?> info(@RequestParam("relativePath") String relativePath) throws Exception {
        return fileService.getFileInfo(relativePath);
    }

    /**
     * Endpoint to compress files.
     *
     * @param files         Map containing a list of relative paths to files for compression.
     * @param request       HttpServletRequest to retrieve the user's token.
     * @return              Result containing the outcome of the compression operation.
     * @throws Exception    Throws an exception if an error occurs during the compression process.
     */
    @ApiOperation("Compress files")
    @Log
    @Permission // Even if allowed, skips files in private directories
    @PostMapping("/zip")
    public Result<?> zip(@RequestBody Map<String, List<String>> files, HttpServletRequest request) throws Exception {
        return fileService.zip(files.get("relativePath"), CookieUtil.getCookie("token", request));
    }

    /**
     * Endpoint to compress and download files.
     *
     * @param files         Map containing a list of relative paths to files for compression and download.
     * @param response      HttpServletResponse to handle the file download.
     * @param request       HttpServletRequest to retrieve the user's token.
     * @throws Exception    Throws an exception if an error occurs during the compression and download process.
     */
    @ApiOperation("Compress and download files")
    @Log
    @Permission // Even if allowed, skips files in private directories
    @PostMapping("/zipAndDownload")
    public void zipAndDownload(@RequestBody Map<String, List<String>> files, HttpServletResponse response, HttpServletRequest request) throws Exception {
        fileService.zipAndDownload(files.get("relativePath"), response, CookieUtil.getCookie("token", request));
    }

    /**
     * Endpoint to generate a direct link for an image.
     *
     * @param relativePath  String representing the relative path of the image.
     * @return              Result containing the direct link for the image.
     * @throws IOException  Throws IOException if an error occurs during link generation.
     */
    @ApiOperation("Generate image link")
    @Log
    @Permission
    @PostMapping("/link")
    public Result<?> link(@RequestParam("relativePath") String relativePath) throws IOException {
        return fileService.generateDirectLink(relativePath);
    }

    /**
     * Endpoint to get a list of images.
     *
     * @param relativePath  String representing the relative path for image retrieval.
     * @return              Result containing the list of images.
     */
    @ApiOperation("Get image list")
    @Log
    @Permission // TODO: Allows getting images from private directories if allowed
    @GetMapping("/gallery")
    public Result<?> getImages(@RequestParam("relativePath") String relativePath) {
        return fileService.getImages(relativePath);
    }

}