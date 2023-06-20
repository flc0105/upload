package flc.upload.controller;


import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.aspect.LogAspect;
import flc.upload.model.AppConfig;
import flc.upload.model.ConfigRequest;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
import flc.upload.util.MyBatisUtil;
import flc.upload.util.ServerInfoUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Api(tags = "配置")
@RestController
@PropertySource("classpath:app-config.properties")
public class ConfigController {
    private final AppConfig config;

    private final MyBatisUtil myBatisUtil;

    public ConfigController(AppConfig config, SqlSession sqlSession, MyBatisUtil myBatisUtil) {
        this.config = config;
        this.myBatisUtil = myBatisUtil;
    }

    @Log
    @Token
    @ApiOperation("修改配置")
    @PostMapping("/config/set")
    public Result updateConfig(@RequestBody ConfigRequest configRequest) {
        String key = configRequest.getKey();
        Object value = configRequest.getValue();
        Field field = CommonUtil.getFieldByName(key, config.getClass());
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(config, value);
                return new Result(true, "修改成功", null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new Result(false, "修改失败：" + e.getMessage(), null);
            }
        } else {
            return new Result(false, "没有找到该配置", null);
        }
    }

    @Log
    @Token
    @ApiOperation("获取配置列表")
    @PostMapping("/config/get")
    public Result getConfig() {
        return new Result(true, "获取成功", CommonUtil.getClassAttributes(config));
    }

    @Log
    @Token
    @ApiOperation("获取日志")
    @PostMapping("/logs")
    public Result getLogs() {
        List<Map<String, String>> logs = LogAspect.logs;
        return new Result(true, "获取成功", logs);
    }

    @Log
    @Token
    @ApiOperation("清空日志")
    @PostMapping("/logs/clear")
    public Result clearLogs() {
        LogAspect.logs.clear();
        return new Result(true, "清空成功", null);
    }

    @Log
    @Token
    @ApiOperation("获取服务器信息")
    @PostMapping("/info")
    public Result getServerInfo() {
        return new Result(true, "获取成功", ServerInfoUtil.getInfoMap());
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

}