package flc.upload.util;

import flc.upload.config.ApplicationStartTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class ServerInfoUtil {
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

    private static String getSQLiteFilePath(String sqliteUrl) {
        // 使用正则表达式解析 SQLite URL 获取文件路径
        Pattern pattern = Pattern.compile("jdbc:sqlite:(.+)");
        Matcher matcher = pattern.matcher(sqliteUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String convertTime(long milliseconds) {
        long millis = milliseconds % 1000;
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / (1000 * 60)) % 60;
        long hours = (milliseconds / (1000 * 60 * 60)) % 24;
        long days = milliseconds / (1000 * 60 * 60 * 24);
        String formattedTime;
        if (days > 0) {
            formattedTime = days + " 天";
        } else if (hours > 0) {
            formattedTime = hours + " 小时";
        } else if (minutes > 0) {
            formattedTime = minutes + " 分钟";
        } else if (seconds > 0) {
            formattedTime = seconds + " 秒";
        } else {
            formattedTime = millis + " 毫秒";
        }
        return formattedTime;
    }

    private static long getDirectorySize(File directory) {
        if (!directory.isDirectory()) {
            return directory.length();
        }
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                size += getDirectorySize(file);
            }
        }
        return size;
    }

    public static Map getInfoMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            map.put("运行端口", port);
            map.put("版本", ServerInfoUtil.class.getPackage().getImplementationVersion());
            map.put("PID", new ApplicationPid().toString());
            map.put("运行时间", convertTime(ApplicationStartTime.getInstance().getUptime()));
            map.put("计算机名", System.getenv("COMPUTERNAME"));
            map.put("用户名", System.getProperty("user.name"));
            map.put("操作系统", System.getProperty("os.name"));
            map.put("操作系统版本", System.getProperty("os.version"));
            map.put("操作系统架构", System.getProperty("os.arch"));
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Object attribute = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "TotalPhysicalMemorySize");
            Object attribute2 = mBeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), "FreePhysicalMemorySize");
            map.put("当前内存占用", FileUtil.formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            map.put("物理内存大小", FileUtil.formatSize(Long.parseLong(attribute.toString())));
            map.put("物理内存剩余", FileUtil.formatSize(Long.parseLong(attribute2.toString())));
            File currentDisk = new File(".");
            map.put("当前磁盘空间", FileUtil.formatSize(currentDisk.getTotalSpace()));
            map.put("当前磁盘剩余空间", FileUtil.formatSize(currentDisk.getFreeSpace()));
            File systemDrive = new File(System.getenv("SystemDrive"));
            map.put("系统所在磁盘剩余空间", FileUtil.formatSize(systemDrive.getFreeSpace()));
            File uploadFolder = new File(uploadPath);
            if (uploadFolder.exists()) {
                map.put("文件上传目录", uploadFolder.getAbsoluteFile());
                map.put("目录总大小", FileUtil.formatSize(getDirectorySize(uploadFolder)));
                map.put("已管理文件个数", FileUtil.countFiles(uploadFolder) + "个文件，" + FileUtil.countFolders(uploadFolder) + "个文件夹");
            }
            File dbFile = new File(Objects.requireNonNull(getSQLiteFilePath(dbPath), "未找到数据库文件"));
            if (dbFile.exists()) {
                map.put("数据库文件路径", dbFile.getAbsoluteFile());
                map.put("数据库大小", FileUtil.formatSize(dbFile.length()));
            }
            map.put("Paste个数", myBatisUtil.executeQueryCount("select count(1) from paste"));
            map.put("书签个数", myBatisUtil.executeQueryCount("select count(1) from bookmark"));
//            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//            long physicalMemorySize = osBean.getTotalPhysicalMemorySize();
//            long usedPhysicalMemorySize = osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize();
//            System.out.println(FileUtil.formatSize(physicalMemorySize));
//            System.out.println(FileUtil.formatSize(usedPhysicalMemorySize));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
