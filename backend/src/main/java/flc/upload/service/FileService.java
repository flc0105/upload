package flc.upload.service;

import flc.upload.model.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface FileService {

    Result<?> upload(MultipartFile[] files, String currentDirectory, String token) throws Exception;

    Result<?> mkdir(String relativePath);

    Result<?> delete(List<String> files) throws Exception;

    Result<?> move(List<String> files, String target);

    Result<?> rename(String relativePath, String target);

    Result<?> list(String currentDirectory, String token);

    Result<?> search(String filter, String currentDirectory, String token);

    void download(String relativePath, HttpServletResponse response) throws Exception;

    void downloadCompressedImage(String relativePath, HttpServletResponse response) throws Exception;

    Result<?> read(String relativePath) throws Exception;

    Result<?> getFileInfo(String relativePath) throws Exception;

    Result<?> zip(List<String> files, String token) throws IOException;

    void zipAndDownload(List<String> files, HttpServletResponse response, String token) throws Exception;

    Result<?> generateDirectLink(String relativePath) throws IOException;

    Result<?> getImages(String relativePath);
}
