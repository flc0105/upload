package flc.upload.service;

import flc.upload.mapper.ShareCodeMapper;
import flc.upload.model.Result;
import flc.upload.model.ShareCode;
import flc.upload.util.FileUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShareCodeService {
    private ShareCodeMapper shareCodeMapper;

    public ShareCodeService(ShareCodeMapper shareCodeMapper) {
        this.shareCodeMapper = shareCodeMapper;
    }

    @Value("${upload.path}")
    private String uploadPath;

    public Result add(String path) throws Exception {
        ShareCode shareCode = shareCodeMapper.findByPath(path);
        if (shareCode != null) {
            return new Result<>(true, null, shareCode.getCode());
        }
        shareCode = new ShareCode();
        shareCode.setCode(RandomStringUtils.randomAlphanumeric(4).toLowerCase());
        shareCode.setPath(path);
        if (shareCodeMapper.add(shareCode) != 0) {
            return new Result<>(true, null, shareCode.getCode());
        } else {
            return new Result<>(false, null);
        }
    }

    public Result delete(String code) throws Exception {
        return new Result<>(shareCodeMapper.delete(code) != 0, null);
    }

    public Result findAll() throws Exception {
        List<ShareCode> shareCodeList = shareCodeMapper.findAll();
        List<ShareCode> newShareCodeList = new ArrayList<>();
        for (ShareCode shareCode: shareCodeList) {
            File file = new File(uploadPath, shareCode.getPath());
            if (file.isFile()) {
                shareCode.setValid(true);
            } else {
                shareCode.setValid(false);
            }
            newShareCodeList.add(shareCode);
        }
        return new Result<>(true, null, newShareCodeList);
    }

    public Result findByCode(String code) throws Exception {
        ShareCode shareCode = shareCodeMapper.findByCode(code);
        if (shareCode != null) {
            File file = new File(uploadPath, shareCode.getPath());
            if (file.isFile()) {
                return new Result<>(true, null, new flc.upload.model.File(file.getName(), file.length(), FileUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
            }
        }
        return new Result<>(false, null);
    }
}
