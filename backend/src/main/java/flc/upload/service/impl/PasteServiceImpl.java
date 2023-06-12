package flc.upload.service.impl;

import flc.upload.manager.TokenManager;
import flc.upload.mapper.PasteMapper;
import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class PasteServiceImpl implements PasteService {
    private PasteMapper pasteMapper;
    private TokenManager tokenManager;

    private Logger logger = LoggerFactory.getLogger(PasteServiceImpl.class);

    public PasteServiceImpl(PasteMapper pasteMapper, TokenManager tokenManager) {
        this.pasteMapper = pasteMapper;
        this.tokenManager = tokenManager;
    }

    public Result add(Paste paste) throws Exception {
        paste.setTime(CommonUtil.getCurrentDate());
        if (pasteMapper.add(paste) != 0) {
            return new Result<>(true, "添加成功", paste);
        } else {
            return new Result<>(false, "添加失败");
        }
    }

    public Result delete(Integer id, String token) throws Exception {
        tokenManager.verify(token);
        if (pasteMapper.delete(id) != 0) {
            return new Result<>(true, "删除成功");
        } else {
            return new Result<>(false, "删除失败");
        }
    }

    public Result update(Paste paste, String token) throws Exception {
        tokenManager.verify(token);
        if (pasteMapper.update(paste) != 0) {
            return new Result<>(true, "修改成功");
        } else {
            return new Result<>(false, "修改失败");
        }
    }

    public Result findAll() throws Exception {
        return new Result<>(true, null, pasteMapper.findAll());
    }

    public Result findById(Integer id) throws Exception {
        Paste paste = pasteMapper.findById(id);
        if (paste != null) {
            if (Objects.equals(paste.getExpiredDate(), "-1")) {
                logger.info("删除" + pasteMapper.delete(id) + "条阅后即焚数据");
            }
            return new Result<>(true, null, paste);
        } else {
            return new Result<>(false, null);
        }
    }

    public Result findLast() throws Exception {
        Paste paste = pasteMapper.findLast();
        if (paste != null) {
            return new Result<>(true, null, paste);
        } else {
            return new Result<>(false, null);
        }
    }

    public Result findUnlisted(String token) throws Exception {
        tokenManager.verify(token);
        List<Paste> unlisted = pasteMapper.findUnlisted();
        return new Result<>(true, null, unlisted);
    }

    public void deleteExpired() throws Exception {
        Integer integer = pasteMapper.deleteExpired(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        logger.info("删除" + integer + "条过期数据");
//        new Result(integer != 0, null);
    }
}
