package flc.upload.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flc.upload.manager.OperationLogManager;
import flc.upload.mapper.OperationLogMapper;
import flc.upload.model.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
public class OperationLogManagerImpl implements OperationLogManager {

    private final OperationLogMapper operationLogMapper;

    @Autowired
    public OperationLogManagerImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public void add(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }

    @Override
    public void deleteById(int id) {
        operationLogMapper.deleteById(id);
    }

    @Override
    public void deleteAll() {
        operationLogMapper.delete(null);
    }

    @Override
    public List<OperationLog> list() {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("operationTime");
        return operationLogMapper.selectList(wrapper);
    }


    public IPage<OperationLog> page(int current, int size) {
        Page<OperationLog> page = new Page<>(current, size);

        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("operationTime");

        return operationLogMapper.selectPage(page, wrapper);
    }
}
