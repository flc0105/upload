package flc.upload.util;

import flc.upload.mapper.SqlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.stereotype.Component;

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
 * 服务器工具类，用于执行服务器相关操作和获取信息。
 */
@Component
public class ServerUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServerUtil.class);
    private static String uploadPath;
    private static String dbPath;
    private static int port;
    private static SqlMapper sqlMapper;

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
    public void setSqlMapper(SqlMapper sqlMapper) {
        ServerUtil.sqlMapper = sqlMapper;
    }

    /**
     * 获取服务器信息。
     *
     * @return 包含服务器信息的映射
     */
    public static Map<String, String> getServerInfo() {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            map.put("port", String.valueOf(port));
            map.put("app.version", ServerUtil.class.getPackage().getImplementationVersion());
            map.put("java.version", System.getProperty("java.version"));
            map.put("pid", new ApplicationPid().toString());
            map.put("uptime", CommonUtil.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime()));
            map.put("hostname", InetAddress.getLocalHost().getHostName());
            map.put("username", System.getProperty("user.name"));
            map.put("operating.system", System.getProperty("os.name"));
            map.put("os.version", System.getProperty("os.version"));
            map.put("os.arch", System.getProperty("os.arch"));
            map.put("memory.usage", FileUtil.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            map.put("total.memory", FileUtil.formatSize(Long.parseLong(ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "TotalPhysicalMemorySize").toString())));
            map.put("free.memory", FileUtil.formatSize(Long.parseLong(ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "FreePhysicalMemorySize").toString())));
//            map.put("total.memory", FileUtil.formatSize(((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize()));
            File file = new File(uploadPath);
            if (file.exists()) {
                map.put("total.space", FileUtil.formatSize(file.getTotalSpace()));
                map.put("free.space", FileUtil.formatSize(file.getFreeSpace()));
                map.put("upload.path", file.getAbsolutePath());
                map.put("directory.size", FileUtil.formatSize(FileUtil.calculateDirectorySize(file)));
                map.put("number.of.managed.files", InternationalizationUtil.translate("file.folder.count", FileUtil.countFiles(file), FileUtil.countFolders(file)));
            }

            File db = new File(Objects.requireNonNull(extractSQLiteFilePath(dbPath)));
            if (db.exists()) {
                map.put("database.path", db.getAbsolutePath());
                map.put("database.size", FileUtil.formatSize(db.length()));
            }

            map.put("number.of.pastes", String.valueOf(sqlMapper.executeQueryCount("paste")));
            map.put("number.of.bookmarks", String.valueOf(sqlMapper.executeQueryCount("bookmark")));
        } catch (Exception e) {
            logger.error("获取服务器信息失败：" + e.getLocalizedMessage());
        }
        return InternationalizationUtil.translateMapKeys(map);
    }

    /**
     * 执行命令。
     *
     * @param command 要执行的命令
     * @return 命令执行结果的字符串
     * @throws IOException          如果发生I/O错误
     * @throws InterruptedException 如果当前线程被中断
     */
    public static String executeCommand(String command) throws IOException, InterruptedException {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.redirectErrorStream(true);
        if (isWindows()) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("sh", "-c", command);
        }
        Process process = processBuilder.start();
        Charset charset = isWindows() ? Charset.forName("GBK") : Charset.defaultCharset();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        logger.info("命令 {} 执行结果：{}", command, process.waitFor() == 0);
        return output.toString();
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

}
