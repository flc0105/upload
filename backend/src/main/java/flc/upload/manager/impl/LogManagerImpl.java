package flc.upload.manager.impl;

import flc.upload.manager.LogManager;
import flc.upload.model.AppConfig;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.SerializationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Service
public class LogManagerImpl implements LogManager {

    public static final String FILENAME = "log.ser";

    public static final List<Map<String, String>> LOGS = SerializationUtil.loadFromFile(FILENAME);



    private static AppConfig appConfig;

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        LogManagerImpl.appConfig = appConfig;
    }


    @Override
    public void add(Map<String, String> map) {
        if (LOGS.size() >= appConfig.getLogMaxSize()) { // 如果日志数量大于配置的数量，从最前面开始删除
            LOGS.remove(0);
        }
        LOGS.add(map); // 添加日志
//        SerializationUtil.saveToFile(LOGS, FILENAME);
    }

    @Override
    public void clear() {
        LOGS.clear();
        SerializationUtil.saveToFile(LOGS, FILENAME);
    }

    @Override
    public List<Map<String, String>> list() {
        return LOGS.stream()
                .map(InternationalizationUtil::translateMapKeys)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> page(int page) {
        int pageSize = 10; // 每页显示的记录数
        // 创建逆转后的日志列表
        List<Map<String, String>> reversedLogs = new ArrayList<>(LOGS);
        Collections.reverse(reversedLogs);

        int totalLogs = reversedLogs.size(); // 总记录数
        int totalPages = (int) Math.ceil((double) totalLogs / pageSize); // 总页数

        // 计算起始索引和结束索引
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalLogs);

        // 获取指定页码的记录
        List<Map<String, String>> pageLogs = reversedLogs.subList(startIndex, endIndex);

        // 对记录进行国际化处理
        pageLogs = pageLogs.stream()
                .map(InternationalizationUtil::translateMapKeys)
                .collect(Collectors.toList());

        // 构建分页结果对象
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("totalPages", totalPages);
        pageResult.put("currentPage", page);
        pageResult.put("data", pageLogs);
        return pageResult;
    }

}
