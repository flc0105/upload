package flc.upload.service;

import flc.upload.model.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public interface FileService {

    Result list(String currentDirectory, String token);

    Result search(String filter, String currentDirectory, String token);

    Result upload(MultipartFile[] files, String currentDirectory, String token) throws Exception;

    Result mkdir(String relativePath);

    Result delete(String files) throws Exception;

    void download(String relativePath, HttpServletResponse response) throws Exception;

    void compress(String relativePath, HttpServletResponse response) throws Exception;

    File zipFolder(String relativePath, String token) throws Exception;

    void downloadFolder(String relativePath, HttpServletResponse response, String token) throws Exception;

    File bulkZip(String files, String token) throws Exception;

    void bulkDownload(String files, HttpServletResponse response, String token) throws Exception;

    Result read(String relativePath) throws Exception;

    Result move(String src, String dst);

    Result rename(String oldName, String newName);

    Result getFileInfo(String relativePath) throws Exception;

    Result generateDirectLink(String relativePath) throws IOException;
}
