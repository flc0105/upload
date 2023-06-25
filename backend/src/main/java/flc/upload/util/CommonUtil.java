package flc.upload.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用工具类，提供常用的通用方法和工具函数。
 */
public class CommonUtil {
    /**
     * 获取当前日期和时间。
     *
     * @return 当前日期和时间的字符串，格式为"yyyy-MM-dd HH:mm:ss"
     */
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 格式化日期。
     *
     * @param date 要格式化的日期，以毫秒为单位的时间戳
     * @return 格式化后的日期字符串，格式为"yyyy-MM-dd HH:mm"
     */
    public static String formatDate(long date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 生成UUID字符串。
     *
     * @return 生成的UUID字符串
     */
    public static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * 将给定的时间转换为人类可读的格式。
     *
     * @param milliseconds 时间，以毫秒为单位
     * @return 转换后的时间字符串
     */
    public static String formatDuration(long milliseconds) {
        // 将时间转换为对应的单位
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        // 计算剩余不足一单位的时间
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours = hours % 24;
        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" ").append(InternationalizationUtil.translate("days")).append(" ");
        }
        if (hours > 0) {
            result.append(hours).append(" ").append(InternationalizationUtil.translate("hours")).append(" ");
        }
        if (minutes > 0) {
            result.append(minutes).append(" ").append(InternationalizationUtil.translate("minutes")).append(" ");
        }
        if (seconds > 0) {
            result.append(seconds).append(" ").append(InternationalizationUtil.translate("seconds")).append(" ");
        }
        return result.toString();
    }

    /**
     * 格式化给定的秒数为 "mm:ss" 的时间表示形式。
     *
     * @param seconds 待格式化的秒数
     * @return 格式化后的时间字符串，格式为 "mm:ss"
     */
    public static String formatSeconds(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        String formattedMinutes = String.format("%02d", minutes);
        String formattedSeconds = String.format("%02d", remainingSeconds);
        return formattedMinutes + ":" + formattedSeconds;
    }

    /**
     * 将Map对象转换为格式化的JSON字符串。
     *
     * @param map 要转换hMap对象
     * @return 格式化的JSON字符串
     * @throws JsonProcessingException 如果转换过程中发生错误
     */
    public static String toJsonString(Map<String, String> map) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.writeValueAsString(map);
    }

    /**
     * 将异常堆栈信息转换为字符串形式
     *
     * @param throwable 异常对象
     * @return 异常堆栈信息的字符串表示
     */
    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
