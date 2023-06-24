package flc.upload.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * 自定义错误页面配置类
 */
@Configuration
public class CustomErrorPageConfig {

    /**
     * 自定义Web服务器工厂配置器，用于添加错误页面处理逻辑
     *
     * @return Web服务器工厂定制器
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> customizer() {
        return factory -> {
            // 定义404错误页面跳转到"/index.html"
            ErrorPage errorPage = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");
            factory.addErrorPages(errorPage);
        };
    }

}
