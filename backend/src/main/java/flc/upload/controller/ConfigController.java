package flc.upload.controller;


import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.aspect.LogAspect;
import flc.upload.model.AppConfig;
import flc.upload.model.ConfigRequest;
import flc.upload.model.Result;
import flc.upload.util.CommonUtil;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;


@RestController
@PropertySource("classpath:app-config.properties")
public class ConfigController {

    private final AppConfig config;

    public ConfigController(AppConfig config) {
        this.config = config;
    }

    @Log
//    @Token
    @PostMapping("/config")
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
    @PostMapping("/logs")
    public Result getLogs() {
        List<Map> logs = LogAspect.logs;

        return new Result(true, "获取成功", logs);

    }




}