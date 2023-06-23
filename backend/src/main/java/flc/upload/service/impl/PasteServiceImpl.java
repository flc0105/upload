package flc.upload.service.impl;

import flc.upload.mapper.PasteMapper;
import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.util.CommonUtil;
import flc.upload.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PasteServiceImpl implements PasteService {
    private final PasteMapper pasteMapper;
    private final Logger logger = LoggerFactory.getLogger(PasteServiceImpl.class);
    public PasteServiceImpl(PasteMapper pasteMapper) {
        this.pasteMapper = pasteMapper;
    }

    public Result<?> add(Paste paste) throws Exception {
        paste.setTime(CommonUtil.getCurrentDate());
        if (pasteMapper.add(paste) != 0) {
            return ResponseUtil.buildSuccessResult("add.success", paste);
        } else {
            return ResponseUtil.buildErrorResult("add.failure");
        }
    }

    public Result<?> delete(Integer id) throws Exception {
        if (pasteMapper.delete(id) != 0) {
            return ResponseUtil.buildSuccessResult("delete.success");
        } else {
            return ResponseUtil.buildErrorResult("delete.failure");
        }
    }

    public Result<?> update(Paste paste) throws Exception {
        if (pasteMapper.update(paste) != 0) {
            return ResponseUtil.buildSuccessResult("update.success");
        } else {
            return ResponseUtil.buildErrorResult("update.failure");
        }
    }

    public Result<?> findAll() throws Exception {
        deleteExpired();
        return ResponseUtil.buildSuccessResult("query.success", pasteMapper.findAll());
    }

    public Result<?> findById(Integer id) throws Exception {
        deleteExpired();
        Paste paste = Objects.requireNonNull(pasteMapper.findById(id), ResponseUtil.getMessage("query.failure"));
//        if (Objects.equals(paste.getExpiredDate(), "-1")) {
//            logger.info("删除 " + pasteMapper.delete(id) + " 条阅后即焚数据");
//        }
        if (paste.isBurnAfterReading()) {
            logger.info("删除 " + pasteMapper.delete(id) + " 条阅后即焚数据");
        }
        return ResponseUtil.buildSuccessResult("query.success", paste);
    }

    public Result<?> findLast() throws Exception {
        deleteExpired();
        return ResponseUtil.buildSuccessResult("query.success", Objects.requireNonNull(pasteMapper.findLast(), ResponseUtil.getMessage("query.failure")));
    }

    public Result<?> findPrivate() throws Exception {
        deleteExpired();
        return ResponseUtil.buildSuccessResult("query.success", pasteMapper.findPrivate());
    }

    public void deleteExpired() throws Exception {
        logger.info("删除 " + pasteMapper.deleteExpired(CommonUtil.getCurrentDate()) + " 条过期数据");
    }
}
