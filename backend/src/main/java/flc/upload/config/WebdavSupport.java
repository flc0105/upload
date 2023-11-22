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

/**
 * WebDAV支持类，继承自WebdavServlet，用于处理WebDAV请求。
 */
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

    /**
     * 初始化方法，配置WebResourceRoot以支持指定路径的资源访问。
     *
     * @throws ServletException 如果初始化过程中发生Servlet异常
     */
    @Override
    public void init() throws ServletException {
        WebResourceRoot webResourceRoot = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
        File path = new File(uploadPath);
        webResourceRoot.addPreResources(new DirResourceSet(webResourceRoot, "/", path.getAbsolutePath(), "/"));
        super.init();
    }

    /**
     * 身份验证方法，用于验证请求的Authorization头中的用户名和密码是否匹配。
     *
     * @param req 请求对象
     * @param res 响应对象
     * @return 如果身份验证通过返回true，否则返回false
     */
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

    /**
     * 重写service方法，对请求进行身份验证后再处理。
     *
     * @param req 请求对象
     * @param res 响应对象
     * @throws ServletException 如果处理过程中发生Servlet异常
     * @throws IOException      如果处理过程中发生I/O异常
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (auth(req, res)) {
            super.service(req, res);
        }
    }

//    @Override
//    protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPropfind(req, resp);
//        System.out.println("doPropfind");
//    }

}