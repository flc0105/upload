package flc.upload.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import flc.upload.manager.OperationLogManager;
import flc.upload.mapper.BookmarkMapper;
import flc.upload.mapper.OperationLogMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Component
@Service
public class OperationLogManagerImpl implements OperationLogManager {
//    public static final List<Map<String, String>> operationLogs = new ArrayList<>();

    private final OperationLogMapper operationLogMapper;

    @Autowired
    public OperationLogManagerImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

//
//    private static AppConfig appConfig;
//
//    @Autowired
//    public void setAppConfig(AppConfig appConfig) {
//        OperationLogManagerImpl.appConfig = appConfig;
//    }

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
        wrapper.orderByAsc("operationTime"); // Assuming "OperationTime" is the field name

        return operationLogMapper.selectList(wrapper);
//        return operationLogMapper.selectList(null);
    }


    public IPage<OperationLog> page(int current, int size) {
        Page<OperationLog> page = new Page<>(current, size);

        // 创建 QueryWrapper
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("operationTime");

        // 这里可以添加其他条件，如果只是分页，可以不添加任何条件
        // wrapper.eq("字段名", "值");

        // 执行分页查询
        return operationLogMapper.selectPage(page, wrapper);
    }
//
//    @Override
//    public void add(Map<String, String> map) {
//        if (operationLogs.size() >= appConfig.getLogMaxSize()) { // 如果日志数量大于配置的数量，从最前面开始删除
//            operationLogs.remove(0);
//        }
//        operationLogs.add(map); // 添加日志
//    }
//
//    @Override
//    public void clear() {
//        operationLogs.clear();
//    }
//
//    @Override
//    public List<Map<String, String>> list() {
//        return operationLogs.stream()
//                .map(InternationalizationUtil::translateMapKeys)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public Map<String, Object> page(int page) {
//        int pageSize = 10; // 每页显示的记录数
//        // 创建逆转后的日志列表
//        List<Map<String, String>> reversedLogs = new ArrayList<>(operationLogs);
//        Collections.reverse(reversedLogs);
//
//        int totalLogs = reversedLogs.size(); // 总记录数
//        int totalPages = (int) Math.ceil((double) totalLogs / pageSize); // 总页数
//
//        // 计算起始索引和结束索引
//        int startIndex = (page - 1) * pageSize;
//        int endIndex = Math.min(startIndex + pageSize, totalLogs);
//
//        // 获取指定页码的记录
//        List<Map<String, String>> pageLogs = reversedLogs.subList(startIndex, endIndex);
//
//        // 对记录进行国际化处理
//        pageLogs = pageLogs.stream()
//                .map(InternationalizationUtil::translateMapKeys)
//                .collect(Collectors.toList());
//
//        // 构建分页结果对象
//        Map<String, Object> pageResult = new HashMap<>();
//        pageResult.put("totalPages", totalPages);
//        pageResult.put("currentPage", page);
//        pageResult.put("data", pageLogs);
//        return pageResult;
//    }

}
