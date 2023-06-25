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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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


    @ApiOperation("日志_查询")
    @Log
    @Permission
    @PostMapping("/logs/list")
    public Result<?> listLogs() {
        List<Map<String, String>> result = LogAspect.logs.stream()
                .map(InternationalizationUtil::translateMapKeys)
                .collect(Collectors.toList());
        return ResponseUtil.buildSuccessResult("query.success", result);
    }

    @ApiOperation("日志_删除")
    @Log
    @Permission
    @PostMapping("/logs/delete")
    public Result<?> deleteLogs() {
        LogAspect.logs.clear();
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