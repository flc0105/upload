package flc.upload.config;


import com.fasterxml.classmate.TypeResolver;
import flc.upload.model.AppConfig;
import flc.upload.model.File;
import flc.upload.model.Folder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger配置类，用于配置Swagger API文档生成工具。
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Autowired
    private TypeResolver typeResolver;


    /**
     * 创建Swagger Docket实例，配置API信息和要扫描的包路径。
     *
     * @return Swagger Docket实例
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("flc.upload"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .additionalModels(typeResolver.resolve(File.class))
                .additionalModels(typeResolver.resolve(Folder.class))
                .additionalModels(typeResolver.resolve(AppConfig.class));
    }

    /**
     * 创建API文档信息。
     *
     * @return API文档信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Upload")
                .description("Upload Application")
                .version("1.0.0")
                .build();
    }
}
