package flc.upload.util;

import flc.upload.config.ApplicationStartTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


@Component
public class ServerInfoUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoUtil.class);
    private static String uploadPath;
    private static String dbPath;
    private static int port;
    private static MyBatisUtil myBatisUtil;

    @Value("${upload.path}")
    public void setUploadPath(String uploadPath) {
        ServerInfoUtil.uploadPath = uploadPath;
    }

    @Value("${spring.datasource.url}")
    public void setDbPath(String dbPath) {
        ServerInfoUtil.dbPath = dbPath;
    }

    @Value("${server.port}")
    public void setPort(int port) {
        ServerInfoUtil.port = port;
    }

    @Autowired
    public void setMyBatisUtil(MyBatisUtil myBatisUtil) {
        ServerInfoUtil.myBatisUtil = myBatisUtil;
    }

    public static Map<String, Object> getInfoMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            map.put("运行端口", port);
            map.put("版本", ServerInfoUtil.class.getPackage().getImplementationVersion());
            map.put("PID", new ApplicationPid().toString());
            map.put("运行时间", CommonUtil.convertTime(ApplicationStartTime.getInstance().getUptime()));
            map.put("主机名", InetAddress.getLocalHost().getHostName());
            map.put("用户名", System.getProperty("user.name"));
            map.put("操作系统", System.getProperty("os.name"));
            map.put("操作系统版本", System.getProperty("os.version"));
            map.put("操作系统架构", System.getProperty("os.arch"));
            map.put("当前内存占用", FileUtil.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            map.put("内存大小", FileUtil.formatSize(CommonUtil.getTotalPhysicalMemorySize()));
            map.put("内存剩余", FileUtil.formatSize(CommonUtil.getFreePhysicalMemorySize()));
            File currentDisk = new File(".");
            map.put("当前磁盘空间", FileUtil.formatSize(currentDisk.getTotalSpace()));
            map.put("当前磁盘剩余空间", FileUtil.formatSize(currentDisk.getFreeSpace()));
            try {
                File systemDrive = new File(System.getenv("SystemDrive"));
                map.put("系统盘剩余空间", FileUtil.formatSize(systemDrive.getFreeSpace()));
            } catch (Exception e) {
                logger.error("获取系统盘剩余空间失败：" + e.getMessage());
            }
            File uploadFolder = new File(uploadPath);
            if (uploadFolder.exists()) {
                map.put("文件上传目录", uploadFolder.getAbsoluteFile());
                map.put("目录大小", FileUtil.formatSize(FileUtil.calculateDirectorySize(uploadFolder)));
                map.put("已管理文件个数", FileUtil.countFiles(uploadFolder) + "个文件，" + FileUtil.countFolders(uploadFolder) + "个文件夹");
            }
            File dbFile = new File(Objects.requireNonNull(CommonUtil.getSQLiteFilePath(dbPath), "未找到数据库文件"));
            if (dbFile.exists()) {
                map.put("数据库路径", dbFile.getAbsoluteFile());
                map.put("数据库大小", FileUtil.formatSize(dbFile.length()));
            }
            map.put("Paste数量", myBatisUtil.executeQueryCount("select count(1) from paste"));
            map.put("书签数量", myBatisUtil.executeQueryCount("select count(1) from bookmark"));
        } catch (Exception e) {
            logger.error("获取服务器信息失败：" + e.getMessage());
        }
        return map;
    }
}
