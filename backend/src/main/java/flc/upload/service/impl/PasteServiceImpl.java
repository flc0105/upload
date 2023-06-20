package flc.upload.service.impl;

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
    private final PasteMapper pasteMapper;

    private final Logger logger = LoggerFactory.getLogger(PasteServiceImpl.class);

    public PasteServiceImpl(PasteMapper pasteMapper) {
        this.pasteMapper = pasteMapper;
    }

    public Result add(Paste paste) throws Exception {
        paste.setTime(CommonUtil.getCurrentDate());
        if (pasteMapper.add(paste) != 0) {
            return new Result<>(true, "添加成功", paste);
        } else {
            return new Result<>(false, "添加失败");
        }
    }

    public Result delete(Integer id) throws Exception {
        if (pasteMapper.delete(id) != 0) {
            return new Result<>(true, "删除成功");
        } else {
            return new Result<>(false, "删除失败");
        }
    }

    public Result update(Paste paste) throws Exception {
        if (pasteMapper.update(paste) != 0) {
            return new Result<>(true, "修改成功");
        } else {
            return new Result<>(false, "修改失败");
        }
    }

    public Result findAll() throws Exception {
        return new Result<>(true, "查询成功", pasteMapper.findAll());
    }

    public Result findById(Integer id) throws Exception {
        Paste paste = pasteMapper.findById(id);
        if (paste != null) {
            if (Objects.equals(paste.getExpiredDate(), "-1")) {
                logger.info("删除" + pasteMapper.delete(id) + "条阅后即焚数据");
            }
            return new Result<>(true, "查询成功", paste);
        } else {
            return new Result<>(false, "查询失败");
        }
    }

    public Result findLast() throws Exception {
        Paste paste = pasteMapper.findLast();
        if (paste != null) {
            return new Result<>(true, "查询成功", paste);
        } else {
            return new Result<>(false, "查询失败");
        }
    }

    public Result findUnlisted() throws Exception {
        List<Paste> unlisted = pasteMapper.findUnlisted();
        return new Result<>(true, "查询成功", unlisted);
    }

    public void deleteExpired() throws Exception {
        Integer integer = pasteMapper.deleteExpired(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        logger.info("删除" + integer + "条过期数据");
    }
}
