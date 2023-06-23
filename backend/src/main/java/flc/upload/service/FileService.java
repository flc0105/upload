package flc.upload.service;

import flc.upload.model.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface FileService {

    Result list(String currentDirectory, String token);

    Result search(String filter, String currentDirectory, String token);

    Result upload(MultipartFile[] files, String currentDirectory, String token) throws Exception;

    Result mkdir(String relativePath);

    Result delete(List<String> files) throws Exception;

    void download(String relativePath, HttpServletResponse response) throws Exception;

    void downloadCompressedImage(String relativePath, HttpServletResponse response) throws Exception;

    Result read(String relativePath) throws Exception;

    Result move(String src, String dst);

    Result<?> move(List<String> files, String target);

    Result<?> rename(String relativePath, String target);

    Result getFileInfo(String relativePath) throws Exception;

    Result generateDirectLink(String relativePath) throws IOException;

    Result<?> zip(List<String> files, String token) throws IOException;

    void zipAndDownload(List<String> files, HttpServletResponse response, String token) throws Exception;
}
