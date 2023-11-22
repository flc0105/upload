package flc.upload.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class EmbeddedServletContainerConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setDocumentRoot(new File(uploadPath));
    }

}