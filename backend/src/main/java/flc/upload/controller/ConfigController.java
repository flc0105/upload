package flc.upload.controller;


import flc.upload.UploadApplication;
import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.mapper.SqlMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.Result;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.ReflectionUtil;
import flc.upload.util.ResponseUtil;
import flc.upload.util.ServerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

@Api(tags = "配置")
@RestController
public class ConfigController {
    private final AppConfig appConfig;

    private final SqlMapper sqlMapper;

    private final ConfigurableApplicationContext context;

    public ConfigController(AppConfig appConfig, SqlMapper sqlMapper, ConfigurableApplicationContext context) {
        this.appConfig = appConfig;
        this.sqlMapper = sqlMapper;
        this.context = context;
    }

    @ApiOperation("获取配置列表")
    @Token
    @PostMapping("/config/list")
    public Result<?> listConfig() {
        return ResponseUtil.buildSuccessResult("query.success", ReflectionUtil.getClassAttributes(appConfig));
    }

    @ApiOperation("修改配置")
    @Log
    @Token
    @PostMapping("/config/update")
    public Result<?> updateConfig(@RequestBody Map<String, Object> params) throws IllegalAccessException {
        String key = (String) params.get("key");
        Object value = params.get("value");
        Field field = Objects.requireNonNull(ReflectionUtil.getFieldByName(key, appConfig.getClass()), InternationalizationUtil.translate("no.such.configuration"));
        field.setAccessible(true);
        field.set(appConfig, value);
        return ResponseUtil.buildSuccessResult("update.success");
    }

    @ApiOperation("SQL查询")
    @Log
    @PostMapping("/sql/select")
    @Token
    public Result<?> select(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("query.success", sqlMapper.executeQuery(sql));
    }

    @ApiOperation("SQL查询数量")
    @Log
    @PostMapping("/sql/count")
    @Token
    public Result<?> count(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("query.success", sqlMapper.executeQueryCount(sql));
    }

    @ApiOperation("SQL更新")
    @Log
    @PostMapping("/sql/execute")
    @Token
    public Result<?> execute(@RequestParam String sql) {
        return ResponseUtil.buildSuccessResult("execute.success", sqlMapper.executeStatement(sql));
    }

    @ApiOperation("关闭服务器")
    @Token
    @PostMapping("/server/shutdown")
    public Result<?> shutdown() {
        new Thread(context::close).start();
        return ResponseUtil.buildSuccessResult("execute.success");
    }

    @ApiOperation("重启服务器")
    @Token
    @PostMapping("/server/restart")
    public Result<?> restart() {
        Thread thread = new Thread(() -> {
            SpringApplication.exit(context, () -> 0);
            SpringApplication.run(UploadApplication.class);
        });
        thread.setDaemon(false);
        thread.start();
        return ResponseUtil.buildSuccessResult("execute.success");
    }

    @ApiOperation("查询服务器信息")
    @Token
    @PostMapping("/server/info")
    public Result<?> getServerInfo() {
        return ResponseUtil.buildSuccessResult("query.success", ServerUtil.getServerInfo());
    }

    @Log
    @ApiOperation("执行命令")
    @PostMapping("/server/execute")
    @Token
    public Result<?> executeCommand(@RequestParam String command) throws IOException, InterruptedException {
        return ResponseUtil.buildSuccessResult("execute.success", ServerUtil.executeCommand(command));
    }

}