package flc.upload.service.impl;

import flc.upload.mapper.ShareCodeMapper;
import flc.upload.model.Result;
import flc.upload.model.ShareCode;
import flc.upload.service.ShareCodeService;
import flc.upload.util.CommonUtil;
import flc.upload.util.FileUtil;
import flc.upload.util.ResponseUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShareCodeServiceImpl implements ShareCodeService {
    private final ShareCodeMapper shareCodeMapper;
    @Value("${upload.path}")
    private String uploadPath;

    public ShareCodeServiceImpl(ShareCodeMapper shareCodeMapper) {
        this.shareCodeMapper = shareCodeMapper;
    }

    public Result<?> add(String path) throws Exception {
        ShareCode shareCode = shareCodeMapper.findByPath(path);
        if (shareCode != null) {
            return ResponseUtil.buildSuccessResult("query.success", shareCode.getCode());
        }
        shareCode = new ShareCode();
        shareCode.setCode(RandomStringUtils.randomAlphanumeric(4).toLowerCase());
        shareCode.setPath(path);
        if (shareCodeMapper.add(shareCode) != 0) {
            return ResponseUtil.buildSuccessResult("add.success", shareCode.getCode());
        } else {
            return ResponseUtil.buildErrorResult("add.failure");
        }
    }

    public Result<?> delete(String code) throws Exception {
        if (shareCodeMapper.delete(code) != 0) {
            return ResponseUtil.buildSuccessResult("delete.success");
        } else {
            return ResponseUtil.buildErrorResult("delete.failure");
        }
    }

    public Result<?> findAll() throws Exception {
        List<ShareCode> list = shareCodeMapper.findAll()
                .stream()
                .peek(shareCode -> shareCode.setValid(new File(uploadPath, shareCode.getPath()).isFile()))
                .collect(Collectors.toList());
        return ResponseUtil.buildSuccessResult("query.success", list);
    }

    public Result<?> findByCode(String code) throws Exception {
        ShareCode shareCode = shareCodeMapper.findByCode(code);
        if (shareCode != null) {
            File file = new File(uploadPath, shareCode.getPath());
            if (file.isFile()) {
                return ResponseUtil.buildSuccessResult("query.success", new flc.upload.model.File(file.getName(), file.length(), CommonUtil.formatDate(file.lastModified()), FileUtil.relativize(uploadPath, file), FileUtil.detectFileType(file)));
            }
        }
        return ResponseUtil.buildErrorResult("query.failure");
    }
}
