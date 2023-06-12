package flc.upload.controller;


import flc.upload.model.AppConfig;
import flc.upload.model.ConfigRequest;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;


@RestController
@PropertySource("classpath:app-config.properties")
public class ConfigController {

    private final AppConfig config;

    public ConfigController(AppConfig config, BeanFactory beanFactory) {
        this.config = config;
    }

    @PostMapping("/config")
    public String updateConfig(@RequestBody ConfigRequest configRequest) {
        String key = configRequest.getKey();
        Object value = configRequest.getValue();
        Field field = getFieldByName(key, config.getClass());
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

    private Field getFieldByName(String fieldName, Class<?> clazz) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getFieldByName(fieldName, superclass);
            }
        }
        return null;
    }
}