package flc.upload.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import flc.upload.UploadApplication;
import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
import flc.upload.annotation.Token;
import flc.upload.manager.OperationLogManager;
import flc.upload.mapper.SqlMapper;
import flc.upload.model.AppConfig;
import flc.upload.model.OperationLog;
import flc.upload.model.Result;
import flc.upload.util.InternationalizationUtil;
import flc.upload.util.ReflectionUtil;
import flc.upload.util.ResponseUtil;
import flc.upload.util.ServerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

@Api(tags = "配置")
@RestController
public class ConfigController {
    private final AppConfig appConfig;

    private final OperationLogManager operationLogManager;

    private final SqlMapper sqlMapper;

    private final ConfigurableApplicationContext context;

    public ConfigController(AppConfig appConfig, OperationLogManager operationLogManager, SqlMapper sqlMapper, ConfigurableApplicationContext context) {
        this.appConfig = appConfig;
        this.operationLogManager = operationLogManager;
        this.sqlMapper = sqlMapper;
        this.context = context;
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

    @ApiOperation("日志_查询所有")
    @Permission
    @GetMapping("/logs/list")
    public Result<?> listLogs() {
        return ResponseUtil.buildSuccessResult("query.success", operationLogManager.list());
    }

    @ApiOperation("日志_分页查询")
    @Permission
    @GetMapping("/logs/page")
    public Result<IPage<OperationLog>> pageLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseUtil.buildSuccessResult("query.success", operationLogManager.page(current, size));
    }

    @ApiOperation("日志_删除")
    @Permission
    @PostMapping("/logs/delete")
    public Result<?> deleteLog(Integer id) {
        operationLogManager.deleteById(id);
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @ApiOperation("日志_清空")
    @Permission
    @PostMapping("/logs/deleteAll")
    public Result<?> deleteAllLogs() {
        operationLogManager.deleteAll();
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

    @ApiOperation("服务器_关闭")
    @Log
    @Token
    @PostMapping("/server/shutdown")
    public Result<?> shutdown() {
        new Thread(context::close).start();
        return ResponseUtil.buildSuccessResult("execute.success");
    }

    @ApiOperation("服务器_重启")
    @Log
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

    @Log
    @ApiOperation("执行shell命令")
    @PostMapping("/shell")
    @Token
    public Result<?> executeCommand(@RequestParam String command) throws IOException, InterruptedException {
        return ResponseUtil.buildSuccessResult("execute.success", ServerUtil.executeCommand(command));
    }

}