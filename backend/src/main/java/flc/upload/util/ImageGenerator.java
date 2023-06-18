package flc.upload.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class ImageGenerator {
    public static String generateImageBase64(char letter) throws IOException {
        // 创建一个50*50的图像
        BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 生成随机的背景颜色
        Color backgroundColor = generateRandomColor();
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, 50, 50);

        // 设置字母的颜色和字体
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));

        // 获取字母的宽度和高度
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int letterWidth = fontMetrics.stringWidth(String.valueOf(letter));
        int letterHeight = fontMetrics.getHeight();

        // 计算字母的位置使其居中显示
        int x = (50 - letterWidth) / 2;
        int y = (50 - letterHeight) / 2 + fontMetrics.getAscent();

        // 在图像上绘制字母
        graphics.drawString(String.valueOf(letter), x, y);
        graphics.dispose();

        // 将图像转换为Base64编码字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        baos.close();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return base64Image;
    }


    private static Color generateRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue);
    }

    public static void main(String[] args) throws IOException {
        char letter = 'A';
        String base64Image = generateImageBase64(letter);
        System.out.println(base64Image);
    }
}
