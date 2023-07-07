package flc.upload.controller;


import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
import flc.upload.annotation.Token;
import flc.upload.aspect.LogAspect;
import flc.upload.mapper.SqlMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.ReflectionUtil;
import flc.upload.util.ResponseUtil;
import flc.upload.util.ServerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "配置")
@RestController
public class ConfigController {
    private final AppConfig appConfig;

    private final SqlMapper sqlMapper;

    public ConfigController(AppConfig appConfig, SqlMapper sqlMapper) {
        this.appConfig = appConfig;
        this.sqlMapper = sqlMapper;
    }

    @ApiOperation("配置_查询")
    @Log
    @Permission
    @PostMapping("/config/list")
    public Result<?> listConfig() {
        return ResponseUtil.buildSuccessResult("query.success", ReflectionUtil.getClassAttributes(appConfig));
    }

    @ApiOperation("配置_修改")
    @Log
    @Permission
    @PostMapping("/config/update")
    public Result<?> updateConfig(@RequestBody Map<String, Object> params) throws IllegalAccessException {
        String key = (String) params.get("key");
        Object value = params.get("value");
        Field field = Objects.requireNonNull(ReflectionUtil.getFieldByName(key, appConfig.getClass()), InternationalizationUtil.translate("no.such.configuration"));
        field.setAccessible(true);
        field.set(appConfig, value);
        return ResponseUtil.buildSuccessResult("update.success");
    }


//    @ApiOperation("日志_查询")
//    @Log
//    @Permission
//    @PostMapping("/logs/list")
//    public Result<?> listLogs() {
//        List<Map<String, String>> result = LogAspect.logs.stream()
//                .map(InternationalizationUtil::translateMapKeys)
//                .collect(Collectors.toList());
//        return ResponseUtil.buildSuccessResult("query.success", result);
//    }

    @ApiOperation("日志_分页查询") //TODO：日志记录过于频繁
    @Log
    @Permission
    @GetMapping("/logs/list")
    public Result<?> listLogs(@RequestParam int page) {
        int pageSize = 10; // 每页显示的记录数
        List<Map<String, String>> allLogs = LogAspect.logs;

        // 创建逆转后的日志列表
        List<Map<String, String>> reversedLogs = new ArrayList<>(allLogs);
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

        return ResponseUtil.buildSuccessResult("query.success", pageResult);
    }

    @ApiOperation("日志_删除")
    @Log
    @Permission
    @PostMapping("/logs/delete")
    public Result<?> deleteLogs() {
        LogAspect.logs.clear();
//        LogAspect.cachedApis.clear();
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @ApiOperation("SQL_查询")
    @Log
    @PostMapping("/sql/select")
    @Token
    public Result<?> select(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("query.success", sqlMapper.executeQuery(sql));
    }

    @ApiOperation("SQL_查询数量")
    @Log
    @PostMapping("/sql/count")
    @Token
    public Result<?> count(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("query.success", sqlMapper.executeQueryCount(sql));
    }

    @ApiOperation("SQL_更新")
    @Log
    @PostMapping("/sql/execute")
    @Token
    public Result<?> execute(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("execute.success", sqlMapper.executeStatement(sql));
    }

    @ApiOperation("查询服务器信息")
    @Log
    @Permission
    @PostMapping("/info")
    public Result<?> getServerInfo() {
        return ResponseUtil.buildSuccessResult("query.success", ServerUtil.getServerInfo());
    }

    @Log
    @ApiOperation("执行shell命令")
    @PostMapping("/shell")
    @Token
    public Result<?> executeCommand(@RequestParam String command) throws IOException, InterruptedException {
        return ResponseUtil.buildSuccessResult("execute.success", ServerUtil.executeCommand(command));
    }

}