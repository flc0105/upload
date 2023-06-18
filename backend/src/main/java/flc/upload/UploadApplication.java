package flc.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@EnableSwagger2
public class UploadApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(UploadApplication.class, args);
        Logger logger = LoggerFactory.getLogger(UploadApplication.class);
        Environment env = applicationContext.getEnvironment();
        try {
            logger.info("\n----------------------------------------------------------\n\t" +
                            "应用 '{}' 启动成功! Access URLs:\n\t" +
                            "Local: \t\thttp://localhost:{}\n\t" +
                            "External: \thttp://{}:{}\n\t" +
                            "Doc: \t\thttp://localhost:{}/swagger-ui/index.html\n" +
                            "----------------------------------------------------------",
                    env.getProperty("spring.application.name"),
                    env.getProperty("server.port"),
                    InetAddress.getLocalHost().getHostAddress(),
                    env.getProperty("server.port"),
                    env.getProperty("server.port"));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
