package flc.upload.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源处理配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 添加静态资源处理器
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/images/");
    }
}
