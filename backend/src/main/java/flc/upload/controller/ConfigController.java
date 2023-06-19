package flc.upload.controller;


import flc.upload.annotation.Log;
import flc.upload.aspect.LogAspect;
import flc.upload.model.AppConfig;
import flc.upload.model.ConfigRequest;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
import flc.upload.util.MyBatisUtil;
import flc.upload.util.ServerInfoUtil;
import org.apache.ibatis.session.SqlSession;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


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
//    @Token
    @PostMapping("/config/set")
    public String updateConfig(@RequestBody ConfigRequest configRequest) {
        String key = configRequest.getKey();
        Object value = configRequest.getValue();
        Field field = CommonUtil.getFieldByName(key, config.getClass());
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(config, value);
                return "修改配置成功";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return "修改配置失败：" + e.getMessage();
            }
        } else {
            return "没有找到该配置";
        }
    }

    @Log
//    @Token
    @PostMapping("/config/get")
    public Map<String, Object> getConfig() {
        return CommonUtil.getClassAttributes(config);
    }

    @Log
//    @Token
    @PostMapping("/logs")
    public Result getLogs() {
        List<Map> logs = LogAspect.logs;
        return new Result(true, "获取成功", logs);
    }

    @Log
//    @Token
    @PostMapping("/info")
    public Result getServerInfo() {
        return new Result(true, "获取成功", ServerInfoUtil.getInfoMap());
    }

    @PostMapping("/sql/select")
    public Result select(@RequestParam String sql) {
        return new Result(true, "查询成功", myBatisUtil.executeQuery(sql));
    }

    @PostMapping("/sql/count")
    public Result count(@RequestParam String sql) {
        return new Result(true, "查询成功", myBatisUtil.executeQueryCount(sql));
    }

    @PostMapping("/sql/execute")
    public Result execute(@RequestParam String sql) {
        return new Result(true, "执行成功", myBatisUtil.executeStatement(sql));
    }


}