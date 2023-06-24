package flc.upload.controller;


import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
import flc.upload.annotation.Token;
import flc.upload.aspect.LogAspect;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSession;
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
//@PropertySource("classpath:app-config.properties")
public class ConfigController {
    private final AppConfig appConfig;

    private final MyBatisUtil myBatisUtil;

    public ConfigController(AppConfig appConfig, SqlSession sqlSession, MyBatisUtil myBatisUtil) {
        this.appConfig = appConfig;
        this.myBatisUtil = myBatisUtil;
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

    @ApiOperation("查询服务器信息")
    @Log
    @Permission
    @PostMapping("/info")
    public Result<?> getServerInfo() {
        return ResponseUtil.buildSuccessResult("query.success", ServerUtil.getServerInfo());
    }






    @Log
    @Token
    @ApiOperation("执行SQL查询")
    @PostMapping("/sql/select")
    public Result select(@RequestParam String sql) {
        return new Result(true, "查询成功", myBatisUtil.executeQuery(sql));
    }

    @Log
    @Token
    @ApiOperation("执行SQL查询个数")
    @PostMapping("/sql/count")
    public Result count(@RequestParam String sql) {
        return new Result(true, "查询成功", myBatisUtil.executeQueryCount(sql));
    }

    @Log
    @Token
    @ApiOperation("执行SQL更新")
    @PostMapping("/sql/execute")
    public Result execute(@RequestParam String sql) {
        return new Result(true, "执行成功", myBatisUtil.executeStatement(sql));
    }

    @Log
    @Token
    @ApiOperation("执行命令")
    @PostMapping("/shell")
    public Result executeCommand(@RequestParam String command) throws IOException, InterruptedException {
        return ServerUtil.executeCommand(command);
    }


}