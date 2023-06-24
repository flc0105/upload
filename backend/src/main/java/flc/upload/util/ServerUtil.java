package flc.upload.util;

import flc.upload.config.ApplicationStartTime;
import flc.upload.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务器工具类，提供对服务器相关信息和操作的封装和常用方法。
 */
@Component
public class ServerUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServerUtil.class);
    private static String uploadPath;
    private static String dbPath;
    private static int port;
    private static MyBatisUtil myBatisUtil;

    public static Map<String, String> getServerInfo() {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            map.put("port", String.valueOf(port));
            map.put("app.version", ServerUtil.class.getPackage().getImplementationVersion());
            map.put("java.version", System.getProperty("java.version"));
            map.put("pid", new ApplicationPid().toString());
            map.put("uptime", CommonUtil.formatDuration(ApplicationStartTime.getInstance().getUptime()));
            map.put("hostname", InetAddress.getLocalHost().getHostName());
            map.put("username", System.getProperty("user.name"));
            map.put("operating.system", System.getProperty("os.name"));
            map.put("os.version", System.getProperty("os.version"));
            map.put("os.arch", System.getProperty("os.arch"));
            map.put("memory.usage", FileUtil.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            map.put("total.memory", FileUtil.formatSize(getTotalPhysicalMemorySize()));
            map.put("free.memory", FileUtil.formatSize(getFreePhysicalMemorySize()));
            File currentDisk = new File(".");
            map.put("total.space", FileUtil.formatSize(currentDisk.getTotalSpace()));
            map.put("free.space", FileUtil.formatSize(currentDisk.getFreeSpace()));
            File uploadFolder = new File(uploadPath);
            if (uploadFolder.exists()) {
                map.put("upload.path", String.valueOf(uploadFolder.getAbsoluteFile()));
                map.put("directory.size", FileUtil.formatSize(FileUtil.calculateDirectorySize(uploadFolder)));
                map.put("number.of.managed.files", InternationalizationUtil.translate("file.folder.count", FileUtil.countFiles(uploadFolder), FileUtil.countFolders(uploadFolder)));
            }
            File dbFile = new File(Objects.requireNonNull(extractSQLiteFilePath(dbPath)));
            if (dbFile.exists()) {
                map.put("database.path", String.valueOf(dbFile.getAbsoluteFile()));
                map.put("database.size", FileUtil.formatSize(dbFile.length()));
            }
            map.put("number.of.pastes", String.valueOf(myBatisUtil.executeQueryCount("select count(1) from paste")));
            map.put("number.of.bookmarks", String.valueOf(myBatisUtil.executeQueryCount("select count(1) from bookmark")));
        } catch (Exception e) {
            logger.error("获取服务器信息失败：" + e.getLocalizedMessage());
        }
        return InternationalizationUtil.translateMapKeys(map);
    }

    /**
     * 执行系统命令并获取输出结果。
     *
     * @param command 要执行的系统命令
     * @return 包含执行结果的对象
     * @throws IOException          如果发生I/O错误
     * @throws InterruptedException 如果执行命令的线程被中断
     */
    public static Result<?> executeCommand(String command) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        if (isWindows()) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }
        Process process = processBuilder.start();
        Charset charset = Charset.defaultCharset();
        if (isWindows()) {
            charset = Charset.forName("GBK");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        int exitCode = process.waitFor();
        return new Result<>(exitCode == 0, "命令执行完成", output.toString());
    }

    /**
     * 判断当前操作系统是否为Windows。
     *
     * @return 如果是Windows操作系统返回true，否则返回false
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    /**
     * 获取操作系统的总物理内存大小（以字节为单位）。
     *
     * @return 操作系统的总物理内存大小
     */
    public static long getTotalPhysicalMemorySize() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return Long.parseLong(mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize").toString());
//        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        long physicalMemorySize = osBean.getTotalPhysicalMemorySize(); //osBean.getFreePhysicalMemorySize();
    }

    /**
     * 获取操作系统的空闲物理内存大小（以字节为单位）。
     *
     * @return 操作系统的空闲物理内存大小
     * @throws Exception 如果获取空闲内存大小失败
     */
    public static long getFreePhysicalMemorySize() throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        return Long.parseLong(mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "FreePhysicalMemorySize").toString());

//        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
//        ObjectName operatingSystemObjectName = new ObjectName("java.lang:type=OperatingSystem");
//        String attributeName = "FreePhysicalMemorySize";
//        Object attributeValue = mBeanServer.getAttribute(operatingSystemObjectName, attributeName);
//        return Long.parseLong(attributeValue.toString());
    }

    /**
     * 从 SQLite 数据库连接字符串中提取数据库文件路径。
     *
     * @param sqliteUrl SQLite 数据库连接字符串
     * @return 数据库文件路径，如果无法提取则返回 null
     */
    public static String extractSQLiteFilePath(String sqliteUrl) {
        Pattern pattern = Pattern.compile("jdbc:sqlite:(.+)");
        Matcher matcher = pattern.matcher(sqliteUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }


    @Value("${upload.path}")
    public void setUploadPath(String uploadPath) {
        ServerUtil.uploadPath = uploadPath;
    }

    @Value("${spring.datasource.url}")
    public void setDbPath(String dbPath) {
        ServerUtil.dbPath = dbPath;
    }

    @Value("${server.port}")
    public void setPort(int port) {
        ServerUtil.port = port;
    }

    @Autowired
    public void setMyBatisUtil(MyBatisUtil myBatisUtil) {
        ServerUtil.myBatisUtil = myBatisUtil;
    }
}
