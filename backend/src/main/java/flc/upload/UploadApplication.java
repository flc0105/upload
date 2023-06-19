package flc.upload;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.util.Objects;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@EnableSwagger2
@EnableOpenApi
@EnableKnife4j
public class UploadApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UploadApplication.class, args);
        Logger logger = LoggerFactory.getLogger(UploadApplication.class);
        Environment env = applicationContext.getEnvironment();
        String port = Objects.equals(env.getProperty("server.port"), "80") ? "" : ":" + env.getProperty("server.port");
        try {
            logger.info("\n----------------------------------------------------------\n\t" +
                            "应用 {} 启动成功\n\t" +
                            "➜ Local: \t\thttp://localhost{}\n\t" +
                            "➜ External: \thttp://{}{}\n\t" +
//                            "Doc: \t\thttp://localhost{}/swagger-ui/index.html\n" +
                            "➜ Doc: \t\thttp://localhost{}/doc.html\n" +
                            "----------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    port,
                    InetAddress.getLocalHost().getHostAddress(),
                    port,
                    port);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
