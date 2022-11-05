package flc.upload.config;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "MyServlet",
        urlPatterns = {"/webdav/*"},
        initParams = {
                @WebInitParam(name = "listings", value = "true"),
                @WebInitParam(name = "readonly", value = "false"),
                @WebInitParam(name = "debug", value = "0")
        })
public class WebdavSupport extends WebdavServlet {
    @Value("${upload.path}")
    private String uploadPath;

    @Value("${webdav.username}")
    private String username;

    @Value("${webdav.password}")
    private String password;

    @Override
    public void init() throws ServletException {
        WebResourceRoot webResourceRoot = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
        File path = new File(uploadPath);
        webResourceRoot.addPreResources(new DirResourceSet(webResourceRoot, "/", path.getAbsolutePath(), "/"));
        super.init();
    }

    private boolean auth(ServletRequest req, ServletResponse res) {
        String authorization = ((HttpServletRequest) req).getHeader("Authorization");
        if (authorization != null) {
            String base64 = authorization.replaceFirst("Basic\\s+", "");
            String string = new String(Base64.decodeBase64(base64), StandardCharsets.UTF_8);
            String[] array = string.split(":");
            if (array.length == 2 && username.equals(array[0]) && password.equals(array[1])) {
                return true;
            }
        }
        HttpServletResponse resp = (HttpServletResponse) res;
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("WWW-Authenticate", "Basic realm=\"DAV\"");
        return false;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (auth(req, res)) {
            super.service(req, res);
        }
    }

}